package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActivityService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private ApplicationUserRepo userRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public HashMap createTransaction(TransactionType transactionType, Transactions transaction,
                                     Account account, GeneralSettings generalSettings) {

        HashMap response = new HashMap();

        try {

            ApplicationUser user = userRepo.findByCode(transaction.getCode());

            if(user == null){
                response.put("message", "No user for this code.");
            }else if(user.isDeleted()){
                response.put("message", "This user is deleted.");
            } else if(!user.getAccountId().equals(account.getId())){
                response.put("message", "This user does not belong to this account loyalty program.");
            } else if(transactionRepo.existsByCheckNumber(transaction.getCheckNumber())){
                response.put("message", "Can't use code for the same check twice.");
            }else {

                ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();

                Optional<Group> groupOptional = groupRepo.findById(user.getGroup().getId());
                if(!groupOptional.isPresent()){
                    response.put("message", "No group for this user.");
                }else if(groupOptional.get().isDeleted()) {
                    response.put("message", "This group is deleted.");
                }else {

                    Group group = groupOptional.get();

                    user.setTop(user.getTop() + 1);
                    group.setTop(group.getTop() + 1);

                    // get discount rate using discount id

                    SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(discounts, group.getSimphonyDiscount().getDiscountId());
                    if(simphonyDiscount.getDiscountId() == 0){
                        response.put("message", "No discount found for this user.");
                        response.put("isSuccess", false);
                        return response;
                    }

                    double discount = simphonyDiscount.getDiscountRate();

                    double amount = transaction.getTotalPayment();
                    double amountAfterDiscount = amount - (amount * (discount / 100));

                    transaction.setUser(user);
                    transaction.setGroup(group);
                    transaction.setDiscountRate(discount);
                    transaction.setAfterDiscount(amountAfterDiscount);
                    transaction.setTransactionDate(new Date());
                    transaction.setTransactionTypeId(transactionType.getId());

                    userRepo.save(user);
                    groupRepo.save(group);

                    transactionRepo.save(transaction);

                    generalSettings.getSimphonyQuota().setUsedTransactionQuota(generalSettings.getSimphonyQuota().getUsedTransactionQuota() + 1);
                    generalSettingsRepo.save(generalSettings);

                    response.put("isSuccess", true);
                    response.put("message", "Discount applied successfully.");
                    response.put("discountId", group.getSimphonyDiscount().getDiscountId());
                    response.put("group", group.getName());
                    response.put("user", user.getName());
                    return response;
                }

                response.put("isSuccess", false);
                return response;
            }

            response.put("isSuccess", false);
            return response;

        }catch(Exception e){
            response.put("isSuccess", false);
            response.put("message", "Can't apply discount.");
            return response;
        }
    }
}

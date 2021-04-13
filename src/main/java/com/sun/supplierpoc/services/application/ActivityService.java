package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private ApplicationUserRepo userRepo;

    @Autowired
    private GroupRepo groupRepo;

    public HashMap createTransaction(TransactionType transactionType, Transactions transaction) {

        HashMap response = new HashMap();

        try {

            ApplicationUser user = userRepo.findByCode(transaction.getCode());

            if(user == null){
                response.put("message", "No user for this code.");
            }else if(user.isDeleted()){
                response.put("message", "This user is deleted.");
            }else if(transactionRepo.existsByCheckNumberAndUser(transaction.getCheckNumber(), user)){
                response.put("message", "Can't use code for the same check twice.");
            }else {

                Optional<Group> groupOptional = groupRepo.findById(user.getGroup().getId());
                if(!groupOptional.isPresent()){
                    response.put("message", "No group for this user.");
                }else if(groupOptional.get().isDeleted()) {
                    response.put("message", "This group is deleted.");
                }else {

                    Group group = groupOptional.get();

                    user.setTop(user.getTop() + 1);
                    group.setTop(group.getTop() + 1);

                    double discount = group.getDiscountRate();
                    double amount = transaction.getTotalPayment();
                    double amountAfterDiscount = amount - (amount * (discount / 100));

                    transaction.setUser(user);
                    transaction.setGroup(group);
                    transaction.setDiscountRate(group.getDiscountRate());
                    transaction.setAfterDiscount(amountAfterDiscount);
                    transaction.setTransactionDate(new Date());
                    transaction.setTransactionTypeId(transactionType.getId());

                    userRepo.save(user);
                    groupRepo.save(group);

                    transactionRepo.save(transaction);

                    response.put("isSuccess", true);
                    response.put("message", "Discount applied successfully.");
                    response.put("discountId", group.getDiscountId());
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

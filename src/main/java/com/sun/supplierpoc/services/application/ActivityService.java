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
            Group group = user.getGroup();

            user.setTop( user.getTop() + 1 );
            group.setTop( group.getTop() + 1 );

            double discount = group.getDiscountRate();
            double amount = transaction.getTotalPayment();
            double amountAfterDiscount = amount - (amount / (discount/100));

            transaction.setUser(user);
            transaction.setGroup(group);
            transaction.setDiscountRate(group.getDiscountRate());
            transaction.setAfterDiscount(amountAfterDiscount);
            transaction.setTransactionDate(new Date());
            transaction.setTransactionTypeId(transactionType.getId());

            transactionRepo.save(transaction);

            response.put("isSuccess", Constants.SUCCESS);
            response.put("message", "Discount applied successfully.");
            response.put("discountId", group.getDiscountId());
            return response;

        }catch(Exception e){
            response.put("isSuccess", Constants.FAILED);
            response.put("message", "Can't apply discount.");
            return response;

        }
    }
}

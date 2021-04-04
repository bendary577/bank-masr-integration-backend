package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepo transactionRepo;

    private Conversions conversion = new Conversions();

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    @Autowired
    private AccountRepo accountRepo;

    public List<Transactions> getTransactionByType(String transactionTypeId) {

            TransactionType transactionType = transactionTypeRepo.findByName(Constants.REDEEM_VOUCHER);

            List<Transactions> transactions = transactionRepo.findAllByTransactionTypeId(transactionType.getId());

            return transactions;

    }

    public void createTransactionType(Account account, TransactionType transactionType) {

        try {

            String endPoint = conversion.toCamelCase(transactionType.getName());
            transactionType.setEndPoint(endPoint);
            transactionType.setCreationDate(new Date());
            transactionType.setDeleted(false);
            transactionType.setAccountId(account.getId());
            transactionTypeRepo.save(transactionType);

        }catch(Exception e){
            LoggerFactory.getLogger(TransactionService.class).info(e.getMessage());
        }
    }

    public double getTotalSpendTransactions(String dateFlag, String transactionTypeName) {

        TransactionType transactionType = transactionTypeRepo.findByName(Constants.REDEEM_VOUCHER);

        List<Transactions> transactions = transactionRepo.findAllByTransactionTypeId(transactionType.getId());

        if(dateFlag.equals("Today")){
            return transactions.get(0).getAfterDiscount();
        }else if(dateFlag.equals("Last Week")){
            return transactions.get(1).getAfterDiscount();
        }else{
            return transactions.get(2).getAfterDiscount();
        }

    }
}

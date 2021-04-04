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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

        List<Transactions> transactions = new ArrayList<>();

        if(dateFlag.equals("Today")){

            double totalSpend = 0;

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EET"), Locale.US);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date date = calendar.getTime();

            transactions = transactionRepo.findByTransactionDateBetween(date, new Date());

            for(Transactions transaction : transactions){
                totalSpend = totalSpend + transaction.getAfterDiscount();
            }

            return totalSpend;

        }else if(dateFlag.equals("Last Week")){

            double totalSpend = 0;

            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
            c.add(Calendar.DATE, -i - 7);
            Date start = c.getTime();
            c.add(Calendar.DATE, 6);
            Date end = c.getTime();

            transactions = transactionRepo.findByTransactionDateBetween(start, end);

            for(Transactions transaction : transactions){
                totalSpend = totalSpend + transaction.getAfterDiscount();
            }

            return totalSpend;
        }else{

            double totalSpend = 0;

            Date date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            int i = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();
            c.add(Calendar.DATE, -i - 30);
            Date start = c.getTime();
            c.add(Calendar.DATE, 29);
            Date end = c.getTime();

            transactions = transactionRepo.findByTransactionDateBetween(start, end);

            for(Transactions transaction : transactions){
                totalSpend = totalSpend + transaction.getAfterDiscount();
            }

            return totalSpend;
        }

    }
}

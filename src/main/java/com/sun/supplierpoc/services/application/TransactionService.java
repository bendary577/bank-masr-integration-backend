package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.simphony.response.TransInRange;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.AccountService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Service
public class TransactionService {

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    private final Conversions conversion = new Conversions();

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    public List<Transactions> getTransactionByType(String transactionTypeId, String time, Account account) {

        TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());
        if (transactionType == null)
            return new ArrayList<>();
        else {
            return getTransactionsByTime(transactionType, time);
        }
    }

    public void createTransactionType(Account account, TransactionType transactionType) {

        try {

            String endPoint = conversion.toCamelCase(transactionType.getName());
            transactionType.setEndPoint(endPoint);
            transactionType.setCreationDate(new Date());
            transactionType.setDeleted(false);
            transactionType.setAccountId(account.getId());
            transactionTypeRepo.save(transactionType);

        } catch (Exception e) {
            LoggerFactory.getLogger(TransactionService.class).info(e.getMessage());
        }
    }

    public double getTotalSpendTransactions(String dateFlag, String transactionTypeName, Account account) {

        TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());

        List<Transactions> transactions;

        double totalSpend = 0;

        transactions = getTransactionsByTime(transactionType, dateFlag);

        for (Transactions transaction : transactions) {
            totalSpend = totalSpend + transaction.getAfterDiscount();
        }

        return totalSpend;

    }

    public TransInRange getTotalSpendTransactionsInRang(String startDate, String endDate, String transactionTypeName, String group, Account account) {

        TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());

        List<Transactions> transactions;

        double totalSpend = 0;

        transactions = getTransactionsByTimeInRAngAndGroup(transactionType, startDate, endDate, group, account);

        for (Transactions transaction : transactions) {
            totalSpend = totalSpend + transaction.getAfterDiscount();
        }

        TransInRange transInRange = new TransInRange(transactions, totalSpend);


        return transInRange;

    }

    public List<Transactions> getTransactionsByTime(TransactionType transactionType, String time) {


        List<Transactions> transactions;

        if (time.equals("Today")) {

            double totalSpend = 0;

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EET"), Locale.US);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date date = calendar.getTime();

            transactions = transactionRepo.findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(transactionType.getId(), date, new Date());

        } else if (time.equals("Last Week")) {

            double totalSpend = 0;

            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, -7);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date start = c.getTime();
            c.add(Calendar.DATE, 7);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date end = c.getTime();

            transactions = transactionRepo.findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(transactionType.getId(), start, end);

        } else if (time.equals("Last Month")) {

            double totalSpend = 0;

            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 0);
            c.add(Calendar.DATE, -30);

            c.add(Calendar.DATE, 0);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date start = c.getTime();

            c.add(Calendar.DATE, 30);

            Month month = Month.of(c.getTime().getMonth() + 1);
            if (month.maxLength() == 31) {
                c.add(Calendar.DATE, 1);
            }

            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date end = c.getTime();

            transactions = transactionRepo.findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(transactionType.getId(), start, end);

        } else {
            transactions = transactionRepo.findAllByTransactionTypeIdOrderByTransactionDateDesc(transactionType.getId());
        }
        return transactions;
    }

    public List<Transactions> getTransactionsByTimeInRAngAndGroup(TransactionType transactionType, String startDate, String endDate, String group, Account account) {

        List<Transactions> transactions = new ArrayList<>();

        Date start = new Date();
        Date end = new Date();

        try {

            if (startDate == null || startDate.equals("") || endDate == null || endDate.equals("") && group != null && group.equals("") ) {

                Optional<Group> transGroupOptional = groupRepo.findByNameAndAccountId(group, account.getId());

                if (transGroupOptional.isPresent()) {

                    Group transGroup = transGroupOptional.get();

                    transactions = transactionRepo.findAllByGroupAndTransactionTypeIdOrderByTransactionDateDesc(transGroup, transactionType.getId());

                }else {
                    return transactions;
                }
            }else {

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                start = df.parse(startDate);

                end = df.parse(endDate);

                if (group != null && !group.equals("")) {

                    Optional<Group> transGroupOptional = groupRepo.findByNameAndAccountId(group, account.getId());

                    if (transGroupOptional.isPresent()) {

                        Group transGroup = transGroupOptional.get();

                        transactions = transactionRepo.findAllByGroupAndTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(transGroup, transactionType.getId(), start, end);

                    } else {
                        return transactions;
                    }

                } else {
                    transactions = transactionRepo.findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(transactionType.getId(), start, end);
                }

            }
        } catch (Exception e) {
            return transactions;
        }

        return transactions;
    }
}

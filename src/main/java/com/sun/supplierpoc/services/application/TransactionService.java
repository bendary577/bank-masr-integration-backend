package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.simphony.response.TransInRange;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    private final Conversions conversion = new Conversions();

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    public int getTransactionByTypeCount(String from, String to, String groupId, Account account) {
        Date fromDate = null;
        Date toDate = null;
        int transactionsCount = 0;
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        try {
            List<TransactionType> transactionTypes = transactionTypeRepo.findByAccountId(account.getId());

            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && to.equals("")){
                to = dateFormat.format(new Date());
            }
            if(!from.equals("") && !to.equals("")){
                // Add one day to range to include last day selected
                Calendar c = Calendar.getInstance();
                c.setTime(dateFormat.parse(to));
                c.add(Calendar.DATE, 1);
                to = dateFormat.format(c.getTime());

                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            }

            if(groupId == null || groupId.equals("")){
                if(fromDate == null && toDate == null){
                    transactionsCount = transactionRepo.countAllByTransactionTypeIn(transactionTypes);
                }else {
                    transactionsCount = transactionRepo.countAllByTransactionTypeInAndTransactionDateBetween(
                            transactionTypes, fromDate, toDate);
                }
            }else {
                Optional<Group> group = groupRepo.findById(groupId);
                if (group != null){
                    if(fromDate == null && toDate == null){
                        transactionsCount = transactionRepo.countAllByTransactionTypeInAndGroup(transactionTypes, group.get());
                    }else {
                        transactionsCount = transactionRepo.countAllByTransactionTypeInAndTransactionDateBetween(
                                transactionTypes, group.get(), fromDate, toDate);
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactionsCount;
    }

    public List<Transactions> getTransactionByTypePaginated(String transactionTypeName, String from, String to,
                                                            String groupId, Account account, int pageNumber, int limit) {
        Date fromDate = null;
        Date toDate = null;
        TransactionType transactionType;
        List<Transactions> transactions = new ArrayList<>();
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        try{
            Pageable paging = PageRequest.of(pageNumber-1, limit);
            if(!from.equals("") && to.equals("")){
                to = dateFormat.format(new Date());
            }
            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && !to.equals("")){
                // Add one day to range to include last day selected
                Calendar c = Calendar.getInstance();
                c.setTime(dateFormat.parse(to));
                c.add(Calendar.DATE, 1);
                to = dateFormat.format(c.getTime());

                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            }

            List<TransactionType> transactionTypes = transactionTypeRepo.findByAccountId(account.getId());

//            transactionType = transactionTypeRepo.findByNameAndAccountId(transactionTypeName, account.getId());

            if(transactionTypes == null || transactionTypes.size() == 0){
                return transactions;
            }

            if(groupId == null || groupId.equals("")){
                if(pageNumber == 0 && limit ==0){
                    if(fromDate == null && toDate == null)
                        transactions = transactionRepo.findAllByTransactionTypeInOrderByTransactionDateDesc(
                                transactionTypes);
                    else
                        transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                                transactionTypes, fromDate, toDate);
                }
                else{
                    if(fromDate == null && toDate == null)
                        transactions = transactionRepo.findAllByTransactionTypeInOrderByTransactionDateDesc(
                                transactionTypes, paging);
                    else
                        transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                                transactionTypes, fromDate, toDate, paging);
                }
            }else {
                Optional<Group> group = groupRepo.findById(groupId);
                if (group != null){
                    if(pageNumber == 0 && limit ==0){
                        if(fromDate == null && toDate == null)
                            transactions = transactionRepo.findAllByTransactionTypeInAndGroupOrderByTransactionDateDesc(
                                    transactionTypes, group.get());
                        else
                            transactions = transactionRepo.
                                    findAllByTransactionTypeInAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(
                                            transactionTypes, group.get(), fromDate, toDate);
                    }
                    else{
                        if(fromDate == null && toDate == null)
                            transactions = transactionRepo.findAllByTransactionTypeInAndGroupOrderByTransactionDateDesc(
                                    transactionTypes, group.get(), paging);
                        else
                            transactions = transactionRepo.findAllByTransactionTypeInAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(
                                    transactionTypes, group.get(), fromDate, toDate, paging);
                    }
                }
            }

        }catch (Exception e){

        }

        return transactions;
    }

    public List<Transactions> getTransactionByTypeStat(String transactionTypeId, String time, Account account,
                                                       int pageNumber, int limit) {
        List<Transactions> transactions;
        List<TransactionType> transactionTypes = transactionTypeRepo.findByAccountId(account.getId());

        if(pageNumber == 0 && limit ==0)
            transactions = getTransactionsByTime(transactionTypes, time);
        else
            transactions = getTransactionsByTimePaginated(transactionTypes, time, pageNumber, limit);


        return getTransactionsByTime(transactionTypes, time);
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

    public HashMap getTotalSpendTransactions(String dateFlag, String transactionTypeName, Account account) {

        HashMap statistic = new HashMap();
        try{
            List<TransactionType> transactionTypes = transactionTypeRepo.findByAccountId(account.getId());
            List<Transactions> transactions;
            double totalSpend = 0;

            transactions = getTransactionsByTime(transactionTypes, dateFlag);

            for (Transactions transaction : transactions) {
                totalSpend = totalSpend + transaction.getAfterDiscount();
            }

            statistic = calculateActivityStatistic(transactions);
            statistic.put("transactions", transactions);
            statistic.put("totalSpend", totalSpend);
        }catch (Exception e){
            e.printStackTrace();
        }

        return statistic;
    }

    public HashMap calculateActivityStatistic(List<Transactions> transactions) {

        HashMap<String, Object> statistic = new HashMap<>();

        List<String> revenues = new ArrayList<>();
        List<Double> expenses = new ArrayList<>();
        String revenue;
        for (int i = 0; i < transactions.size(); i++) {
            revenue = transactions.get(i).getRevenueCentreName();

            if (notExistInRevenues(revenue, revenues)) {
                double expense = 0;
                for (int j = 0; j < transactions.size(); j++) {
                    if (transactions.get(j).getRevenueCentreName().equals(revenue) && !revenue.equals("")) {
                        expense = expense + transactions.get(j).getAfterDiscount();
                    }
                }
                revenues.add(revenue);
                expenses.add(expense);
            }
        }
        statistic.put("revenues", revenues);
        statistic.put("expenses", expenses);
        statistic = grtTopRevenues(revenues, expenses, statistic);
        if(revenues.size() == 1 || revenues.size() == 2){
            revenues.addAll(Arrays.asList("", ""));
        }
        return statistic;
    }

    public HashMap grtTopRevenues(List<String> revenues, List<Double> expenses, HashMap statistic){
        double revenueLength = revenues.size();
        if (revenueLength > 5) { revenueLength = 5; }
        revenues = revenues.stream().limit(5).collect(Collectors.toList());
        expenses = expenses.stream().limit(5).collect(Collectors.toList());
//        if (revenueLength > 0) {
//            if (revenueLength > 3) { revenueLength = 3 }
//            var topValues = tempExpence.sort((a, b) => b - a).slice(0, length);
//            this.topRevenueCenters = [tempRevenue[tempExpence.indexOf(topValues[0])],
//                    tempRevenue[tempExpence.indexOf(topValues[1])], tempRevenue[tempExpence.indexOf(topValues[2])]]
//            this.topRevenueCenters = this.topRevenueCenters.sort((a, b) => b - a).slice(0, length);
//        }
        statistic.put("topRevenueCenters", revenues);
        return statistic;
    }

    public boolean notExistInRevenues(String revenue, List<String> revenues) {
        for (int i = 0; i < revenues.size(); i++) {
            if (revenues.get(i).equals(revenue)) {
                return false;
            }
        }
        return true;
    }

    public TransInRange getTotalSpendTransactionsInRang(String startDate, String endDate, String transactionTypeName, String group, Account account) {
        List<TransactionType> transactionTypes = transactionTypeRepo.findByAccountId(account.getId());
        List<Transactions> transactions;
        double totalSpend = 0;
        transactions = getTransactionsByTimeInRAngAndGroup(transactionTypes, startDate, endDate, group, account);
        for (Transactions transaction : transactions) {
            totalSpend = totalSpend + transaction.getAfterDiscount();
        }
        TransInRange transInRange = new TransInRange(transactions, totalSpend);
        return transInRange;
    }

    public List<Transactions> getTransactionsByTime(List<TransactionType> transactionTypes, String time) {
        List<Transactions> transactions;

        if (time.equals("Today")) {
            double totalSpend = 0;
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EET"), Locale.US);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date date = calendar.getTime();
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                    transactionTypes, date, new Date());
        }
        else if (time.equals("Last Week")) {
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
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transactionTypes, start, end);
        }
        else if (time.equals("Last Month")) {
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
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transactionTypes, start, end);
        }
        else {
            transactions = transactionRepo.findAllByTransactionTypeInOrderByTransactionDateDesc(transactionTypes);
        }
        return transactions;
    }

    public List<Transactions> getTransactionsByTimePaginated(List<TransactionType> transactionTypes, String time,
                                                    int pageNumber, int limit) {
        List<Transactions> transactions;

        Pageable paging = PageRequest.of(pageNumber-1, limit);

        if (time.equals("Today")) {
            double totalSpend = 0;
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EET"), Locale.US);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date date = calendar.getTime();
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                    transactionTypes, date, new Date());
        }
        else if (time.equals("Last Week")) {
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
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                    transactionTypes, start, end);
        }
        else if (time.equals("Last Month")) {
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
            transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(
                    transactionTypes, start, end);
        }
        else {
            transactions = transactionRepo.findAllByTransactionTypeInOrderByTransactionDateDesc(transactionTypes);
        }
        return transactions;
    }

    public List<Transactions> getTransactionsByTimeInRAngAndGroup(List<TransactionType> transactionTypes, String startDate, String endDate, String groupId, Account account) {
        List<Transactions> transactions = new ArrayList<>();
        Date start = new Date();
        Date end = new Date();
        try {
            if (startDate == null || startDate.equals("") || endDate == null || endDate.equals("") && groupId != null && groupId.equals("") ) {
                Optional<Group> transGroupOptional = groupRepo.findByIdAndAccountId(groupId, account.getId());
                if (transGroupOptional.isPresent()) {
                    Group transGroup = transGroupOptional.get();
                    transactions = transactionRepo.findAllByGroupIdAndTransactionTypeInOrderByTransactionDateDesc(transGroup.getId(), transactionTypes);
                }else {
                    return transactions;
                }
            }else {
                DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
                start = df.parse(startDate.substring(0, 23));
                end = new Date(df.parse(endDate.substring(0, 23)).getTime() + MILLIS_IN_A_DAY);
                if (groupId != null && !groupId.equals("")) {
                    Optional<Group> transGroupOptional = groupRepo.findByIdAndAccountId(groupId, account.getId());
                    if (transGroupOptional.isPresent()) {
                        Group transGroup = transGroupOptional.get();
                        transactions = transactionRepo.findAllByGroupIdAndTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transGroup.getId(), transactionTypes, start, end);
                    } else {
                        return transactions;
                    }
                } else {
                    transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transactionTypes, start, end);
                }
            }
        } catch (Exception e) {
            return transactions;
        }
        return transactions;
    }

    public String updateTransactions(){
        List<Transactions> transactionList = transactionRepo.findAll();
        TransactionType transactionType;
        for(Transactions transaction: transactionList){
            transactionType = transactionTypeRepo.findById(transaction.getTransactionTypeId())
            .orElseThrow();
            transaction.setTransactionType(transactionType);
            transactionRepo.save(transaction);
        }

        return "";
    }
}

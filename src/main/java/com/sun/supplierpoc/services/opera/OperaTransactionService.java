package com.sun.supplierpoc.services.opera;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.OperaTransaction;
import com.sun.supplierpoc.repositories.opera.OperaTransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OperaTransactionService {

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    @Autowired
    private OperaTransactionRepo operaTransactionRepo;


    public HashMap<String, Object> filterTransactionsAndCalculateTotals(String startDate, String endDate, String cardNumber,
                                                                        Account account) {

        HashMap<String, Object> response = new HashMap();

        double succeedTransactionCount = 0;
        double failedTransactionCount = 0;
        int totalTransactionAmount = 0;

        List<OperaTransaction> transactions = filterTransactions(startDate, endDate, cardNumber, account);

        for (OperaTransaction transaction : transactions) {
            if(transaction.getStatus().equals("Success")) {
                succeedTransactionCount += 1;
            }else {
                failedTransactionCount += 1;
            }
            totalTransactionAmount += transaction.getAmount();
        }

        response.put("succeedTransactionCount", succeedTransactionCount);
        response.put("failedTransactionCount", failedTransactionCount);
        response.put("totalTransactionAmount", totalTransactionAmount);
        response.put("transactions", transactions);

        return response;

    }

    public List<OperaTransaction> filterTransactions(String startDate, String endDate, String cardNumber, Account account) {
        List<OperaTransaction> operaTransactions = new ArrayList<>();
        Date start, end;
        try {
            if ((startDate == null || startDate.equals("") || startDate == null || endDate.equals("")) && cardNumber != null && !cardNumber.equals("")){
                operaTransactions = operaTransactionRepo.findAllByAccountIdAndDeletedAndCardNumber(account.getId(), false, cardNumber);
            }else if((startDate != null || !startDate.equals("") || startDate != null || !endDate.equals(""))){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    start = dateFormat.parse(startDate);
                    end = new Date(dateFormat.parse(endDate).getTime() + MILLIS_IN_A_DAY);
                }catch(Exception e){
                    dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
                    start= dateFormat.parse(startDate.substring(0, 24));
                    end = dateFormat.parse(endDate.substring(0, 24));
                }
                if(cardNumber.equals("") || cardNumber == null){
                    operaTransactions = operaTransactionRepo.findByAccountIdAndDeletedAndCreationDateBetween(
                            account.getId(), false, start, end);
                }else{
                    operaTransactions = operaTransactionRepo.findByAccountIdAndDeletedAndCardNumberAndCreationDateBetween(account.getId(), false, cardNumber,start, end);
                }
            }else if(cardNumber.equals("") || cardNumber == null){
                    operaTransactions = operaTransactionRepo.findAllByAccountIdAndDeletedAndCardNumber(account.getId(), false, cardNumber);
            }
            return operaTransactions;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


//    List<Transactions> transactions = new ArrayList<>();
//    Date start = new Date();
//    Date end = new Date();
//        try {
//        if (startDate == null || startDate.equals("") || endDate == null || endDate.equals("") && groupId != null && groupId.equals("") ) {
//            Optional<Group> transGroupOptional = groupRepo.findByIdAndAccountId(groupId, account.getId());
//            if (transGroupOptional.isPresent()) {
//                Group transGroup = transGroupOptional.get();
//                transactions = transactionRepo.findAllByGroupIdAndTransactionTypeInOrderByTransactionDateDesc(transGroup.getId(), transactionTypes);
//            }else {
//                return transactions;
//            }
//        }else {
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            start = df.parse(startDate);
//            end = new Date(df.parse(endDate).getTime() + MILLIS_IN_A_DAY);
//            if (groupId != null && !groupId.equals("")) {
//                Optional<Group> transGroupOptional = groupRepo.findByIdAndAccountId(groupId, account.getId());
//                if (transGroupOptional.isPresent()) {
//                    Group transGroup = transGroupOptional.get();
//                    transactions = transactionRepo.findAllByGroupIdAndTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transGroup.getId(), transactionTypes, start, end);
//                } else {
//                    return transactions;
//                }
//            } else {
//                transactions = transactionRepo.findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(transactionTypes, start, end);
//            }
//        }
//    } catch (Exception e) {
//        return transactions;
//    }
//        return transactions;
//}

}

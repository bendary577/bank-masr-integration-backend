package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
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
                                     Account account, GeneralSettings generalSettings, RevenueCenter revenueCenter) {

        HashMap response = new HashMap();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        try {
            ApplicationUser user = userRepo.findByCodeAndAccountIdAndDeleted(transaction.getCode(), account.getId(), false);
            transaction.setAccountId(account.getId());

            if(user == null){
                response.put("message", "No user for this code.");
            }else if(user.isDeleted()){
                response.put("message", "This user is deleted.");
            } else if(!user.getAccountId().equals(account.getId())){
                response.put("message", "This user does not belong to this account loyalty program.");
            } else if(transactionRepo.existsByCheckNumberAndRevenueCentreIdAndStatusAndAccountId(
                    transaction.getCheckNumber(), transaction.getRevenueCentreId(), Constants.PAID_TRANSACTION, account.getId())){
                response.put("message", "Can't use code for the same check twice.");
            }
            else {
                Optional<Group> groupOptional = groupRepo.findById(user.getGroup().getId());
                if(!groupOptional.isPresent()){
                    response.put("message", "No group for this user.");
                }else if(groupOptional.get().isDeleted()) {
                    response.put("message", "This group is deleted.");
                }else {
                    Group group = groupOptional.get();

                    user.setTop(user.getTop() + 1);
                    group.setTop(group.getTop() + 1);

                    transaction.setUser(user);
                    transaction.setGroup(group);
                    transaction.setTransactionDate(new Date());
                    transaction.setTransactionTypeId(transactionType.getId());
                    transaction.setTransactionType(transactionType);

                    double amount = transaction.getTotalPayment();

                    /*
                    * pointReward = 0; // percentage
                    * pointsRedemption = 0; // 1$ = ? points
                    * */
                    if(transactionType.getName().equals(Constants.POINTS_REDEMPTION)){
                        int points = (int) Math.round(transaction.getTotalPayment() * generalSettings.getPointsRedemption());

                        if(points > user.getPoints()){
                            response.put("message", "There are insufficient points to redeem.");
                            response.put("isSuccess", false);
                            return response;
                        }

                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(Double.parseDouble(decimalFormat.format(transaction.getTotalPayment())));
                        transaction.setPointsRedeemed(points);

                        user.setPoints(user.getPoints() - points);
                    }
                    else if(transactionType.getName().equals(Constants.REWARD_POINTS)){
                        int points = (int) Math.round((transaction.getTotalPayment() * generalSettings.getPointReward())/100);

                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(Double.parseDouble(decimalFormat.format(transaction.getTotalPayment())));
                        transaction.setPointsRedeemed(0);
                        transaction.setPointsReward(points);
                        user.setPoints(user.getPoints() + points);
                    }
                    else if(!transactionType.getName().equals(Constants.USE_WALLET) && !transactionType.getName().equals(Constants.CANTEEN)) {
                        // get discount rate using discount id
                        ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();
                        SimphonyDiscount simphonyDiscount = conversions.checkSimphonyDiscountExistence(discounts, group.getSimphonyDiscount().getDiscountId());
                        if(simphonyDiscount.getDiscountId() == 0){
                            response.put("message", "No discount found for this user.");
                            response.put("isSuccess", false);
                            return response;
                        }
                        double discount = simphonyDiscount.getDiscountRate();
                        double amountAfterDiscount = amount - (amount * (discount / 100));
                        transaction.setDiscountRate(discount);
                        transaction.setAfterDiscount(Double.parseDouble(decimalFormat.format(amountAfterDiscount)));
                        transaction.setStatus(Constants.PAID_TRANSACTION);

                        response.put("message", "Discount added successfully.");
                    }
                    else if(transactionType.getName().equals(Constants.USE_WALLET)){
                        if(user.getWallet() == null){
                            response.put("isSuccess", false);
                            response.put("message", "This user is not a member of the wallet system.");
                            return response;
                        }

                        /* Check if user expired or deleted */
                        if(user.getExpiryDate() == null || new Date().compareTo(user.getExpiryDate()) >= 0
                                || user.isSuspended() || user.isDeleted()){
                            response.put("isSuccess", false);
                            response.put("message", "This user is currently unable to use the wallet system.");
                            return response;
                        }

                        /* check if account has loyalty system */
                        transaction.setAfterDiscount(Double.parseDouble(decimalFormat.format(transaction.getTotalPayment())));

                        Wallet wallet = user.getWallet();
                        double previousBalance = calculateBalance(wallet);
                        double rest = transaction.getAfterDiscount();
                        double newBalance = 0;
                        double paidAmount = 0;

                        for(int i = 0; i <  wallet.getBalance().size(); i ++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;

                            if(conversions.containRevenueCenter(wallet.getBalance().get(i), revenueCenter)){
                                if(wallet.getBalance().get(i).getAmount() >= rest) {
                                    newBalance = wallet.getBalance().get(i).getAmount() - rest;
                                    wallet.getBalance().get(i).setAmount(newBalance);
                                    rest = 0;
                                    break;
                                }else{
                                    rest -= wallet.getBalance().get(i).getAmount();
                                    wallet.getBalance().get(i).setAmount(0);
                                    continue;
                                }
                            }
                        }
                        paidAmount = transaction.getAfterDiscount() - rest;
                        transaction.setPartialPayment(Double.parseDouble(decimalFormat.format(paidAmount)));

                        /* Remove zero balance/voucher */
                        List<Balance> newBalanceList = new ArrayList<>();
                        for(int i = 0; i <  wallet.getBalance().size(); i ++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;
                            else
                                newBalanceList.add(wallet.getBalance().get(i));
                        }
                        wallet.setBalance(newBalanceList);

                        newBalance = calculateBalance(wallet);
                        WalletHistory walletHistory = new WalletHistory("Use wallet in " + revenueCenter.getRevenueCenter(),
                                paidAmount, previousBalance, newBalance, null, new Date());
                        walletHistory.setActionId(UUID.randomUUID().toString());
                        walletHistory.setEmployee(transaction.getEmployeeId());
                        walletHistory.setCheck(transaction.getCheckNumber());
                        wallet.getWalletHistory().add(walletHistory);
                        user.setWallet(wallet);

                        if(rest == transaction.getAfterDiscount()){
                            response.put("rest", conversions.roundUpDouble(transaction.getAfterDiscount()));
                            transaction.setStatus(Constants.INSUFFICIENT_AMOUNT);
                            response.put("message", user.getName() + " has no balance to spend at this revenue center.");
                        }else if(rest != 0){
                            transaction.setStatus(Constants.PARTIAL_PAYMENT);
                            response.put("message", "Payment added successfully.");
                        }else if(rest == 0){
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                            response.put("message", "Check paid successfully.");
                        }else {
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                        }

                        response.put("rest", conversions.roundUpDouble(rest));
                        response.put("newBalance", conversions.roundUpDouble(newBalance));
                        response.put("paidAmount", conversions.roundUpDouble(paidAmount));
                    }
                    else if(transactionType.getName().equals(Constants.CANTEEN)){
                        if(!group.getCanteenConfiguration().isIncludeFees()){
                            amount = transaction.getNetAmount();
                            response.put("fees", false);
                        }else{
                            response.put("fees", true);
                        }

                        if(user.getWallet() == null){
                            response.put("isSuccess", false);
                            response.put("message", "This user is not a member of the canteen system.");
                            return response;
                        }

                        /* check if account has loyalty feature */
                        if(group.getSimphonyDiscount().getDiscountId() != 0){
                            ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();
                            SimphonyDiscount simphonyDiscount =
                                    conversions.checkSimphonyDiscountExistence(discounts, group.getSimphonyDiscount().getDiscountId());

                            double netAmount = transaction.getNetAmount();
                            double discount = simphonyDiscount.getDiscountRate();
                            double amountAfterDiscount = netAmount - Math.round(netAmount * (discount / 100));

                            transaction.setDiscountRate(discount);

                            if(!group.getCanteenConfiguration().isIncludeFees()){
                                transaction.setAfterDiscount(amountAfterDiscount);
                            }else {
                                // Calculate service charge and vat after discount
                                double newServiceCharges = Math.round((transaction.getServiceCharges() * amountAfterDiscount)/netAmount);
                                double newVat = Math.round((transaction.getVat() * amountAfterDiscount)/netAmount);
                                transaction.setAfterDiscount(amountAfterDiscount +
                                        newServiceCharges + newVat);
                            }

                        }else {
                            transaction.setAfterDiscount(amount);
                        }

                        Wallet wallet = user.getWallet();
                        double previousBalance = calculateBalance(wallet);
                        double rest = transaction.getAfterDiscount();
                        double newBalance = 0;
                        double paidAmount = 0;

                        for(int i = 0; i <  wallet.getBalance().size(); i++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;

                            if(conversions.containRevenueCenter(wallet.getBalance().get(i), revenueCenter)){
                                if(wallet.getBalance().get(i).getAmount() >= rest) {
                                    newBalance = wallet.getBalance().get(i).getAmount() - rest;
                                    wallet.getBalance().get(i).setAmount(newBalance);
                                    rest = 0;
                                    break;
                                }else{
                                    rest -= wallet.getBalance().get(i).getAmount();
                                    wallet.getBalance().get(i).setAmount(0);
                                    continue;
                                }
                            }
                        }
                        paidAmount = transaction.getAfterDiscount() - rest;
                        transaction.setPartialPayment(Double.parseDouble(decimalFormat.format(paidAmount)));

                        /* Remove zero balance/voucher */
                        List<Balance> newBalanceList = new ArrayList<>();
                        for(int i = 0; i <  wallet.getBalance().size(); i ++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;
                            else
                                newBalanceList.add(wallet.getBalance().get(i));
                        }
                        wallet.setBalance(newBalanceList);

                        newBalance = calculateBalance(wallet);
                        WalletHistory walletHistory = new WalletHistory("Use wallet in " + revenueCenter.getRevenueCenter(),
                                paidAmount, previousBalance, newBalance, null, new Date());
                        walletHistory.setActionId(UUID.randomUUID().toString());
                        walletHistory.setEmployee(transaction.getEmployeeId());
                        walletHistory.setCheck(transaction.getCheckNumber());
                        wallet.getWalletHistory().add(walletHistory);
                        user.setWallet(wallet);

                        if(rest == transaction.getAfterDiscount()){
                            response.put("rest", conversions.roundUpDouble(transaction.getAfterDiscount()));
                            transaction.setStatus(Constants.INSUFFICIENT_AMOUNT);
                            response.put("message", user.getName() + " has no balance to spend at this revenue center.");
                        }else if(rest != 0){
                            transaction.setStatus(Constants.PARTIAL_PAYMENT);
                            response.put("message", "Payment added successfully.");
                        }else if(rest == 0){
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                            response.put("message", "Check paid successfully.");
                        }else {
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                        }

                        response.put("rest", conversions.roundUpDouble(rest));
                        response.put("newBalance", conversions.roundUpDouble(newBalance));
                        response.put("paidAmount", conversions.roundUpDouble(paidAmount));
                    }
                    else{
                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(Double.parseDouble(decimalFormat.format(transaction.getTotalPayment())));
                    }

                    userRepo.save(user);
                    groupRepo.save(group);
                    transactionRepo.save(transaction);

                    generalSettings.getSimphonyQuota().setUsedTransactionQuota(generalSettings.getSimphonyQuota().getUsedTransactionQuota() + 1);
                    generalSettingsRepo.save(generalSettings);

                    if(transactionType.getName().equals(Constants.REWARD_POINTS)
                            || transactionType.getName().equals(Constants.POINTS_REDEMPTION)){
                        response.put("message", "New balance = " + user.getPoints());
                    }

                    response.put("isSuccess", true);
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
            response.put("message", "This is an error occurs, Please contact support team.");
            return response;
        }
    }

    public HashMap createCanteenTransaction(TransactionType transactionType, Transactions transaction,
                                     Account account, GeneralSettings generalSettings, RevenueCenter revenueCenter) {

        HashMap response = new HashMap();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        try {
            ApplicationUser user = userRepo.findByCodeAndAccountIdAndDeleted(transaction.getCode(), account.getId(), false);

            if(user.getWallet() == null){
                response.put("isSuccess", false);
                response.put("message", "This user is not a member of the canteen system.");
                return response;
            }

            Optional<Group> groupOptional = groupRepo.findById(user.getGroup().getId());
            Group group = groupOptional.get();

            transaction.setUser(user);
            transaction.setGroup(group);
            transaction.setTransactionDate(new Date());
            transaction.setTransactionTypeId(transactionType.getId());
            transaction.setTransactionType(transactionType);
            transaction.setAccountId(account.getId());

            // Edit Stats
            user.setTop(user.getTop() + 1);
            group.setTop(group.getTop() + 1);

            double netAmount = transaction.getNetAmount();
            double vat = transaction.getVat();
            double serviceCharge = transaction.getServiceCharges();
            double discount = transaction.getDiscount();
            double totalPayment;

            if(!group.getCanteenConfiguration().isIncludeFees()){
                totalPayment = netAmount + discount;
                response.put("fees", false);
            }else{
                totalPayment = (netAmount + vat + serviceCharge) + discount;
                response.put("fees", true);
            }
            transaction.setAfterDiscount(totalPayment);

            Wallet wallet = user.getWallet();
            double previousBalance = calculateBalance(wallet);
            double rest = totalPayment;
            double newBalance = 0;
            double paidAmount = 0;

            if(discount != 0){
                ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();
                SimphonyDiscount simphonyDiscount =
                        conversions.checkSimphonyDiscountExistence(discounts, group.getSimphonyDiscount().getDiscountId());

                transaction.setDiscountRate(simphonyDiscount.getDiscountRate());
            }
            transaction.setDiscount(discount);

            for(int i = 0; i <  wallet.getBalance().size(); i++){
                if(wallet.getBalance().get(i).getAmount() == 0)
                    continue;

                if(conversions.containRevenueCenter(wallet.getBalance().get(i), revenueCenter)){
                    if(wallet.getBalance().get(i).getAmount() >= rest) {
                        newBalance = wallet.getBalance().get(i).getAmount() - rest;
                        wallet.getBalance().get(i).setAmount(newBalance);
                        rest = 0;
                        break;
                    }else{
                        rest -= wallet.getBalance().get(i).getAmount();
                        wallet.getBalance().get(i).setAmount(0);
                        continue;
                    }
                }
            }
            paidAmount = transaction.getAfterDiscount() - rest;
            transaction.setPartialPayment(Double.parseDouble(decimalFormat.format(paidAmount)));

            /* Remove zero balance/voucher */
            List<Balance> newBalanceList = new ArrayList<>();
            for(int i = 0; i <  wallet.getBalance().size(); i ++){
                if(wallet.getBalance().get(i).getAmount() == 0)
                    continue;
                else
                    newBalanceList.add(wallet.getBalance().get(i));
            }
            wallet.setBalance(newBalanceList);

            newBalance = calculateBalance(wallet);

            if(paidAmount > 0){
                WalletHistory walletHistory = new WalletHistory("Use wallet in " + revenueCenter.getRevenueCenter(),
                        paidAmount, previousBalance, newBalance, null, new Date());
                walletHistory.setActionId(UUID.randomUUID().toString());
                walletHistory.setEmployee(transaction.getEmployeeId());
                walletHistory.setCheck(transaction.getCheckNumber());
                wallet.getWalletHistory().add(walletHistory);
            }

            user.setWallet(wallet);

            if(rest == transaction.getAfterDiscount()){
                response.put("rest", conversions.roundUpDouble(transaction.getAfterDiscount()));
                transaction.setStatus(Constants.INSUFFICIENT_AMOUNT);
                response.put("message", user.getName() + " has no balance to spend at this revenue center.");
            }else if(rest != 0){
                transaction.setStatus(Constants.PARTIAL_PAYMENT);
                response.put("message", "Payment added successfully.");
            }else if(rest == 0){
                transaction.setStatus(Constants.PAID_TRANSACTION);
                response.put("message", "Check paid successfully.");
            }else {
                transaction.setStatus(Constants.PAID_TRANSACTION);
            }

            userRepo.save(user);
            groupRepo.save(group);
            transactionRepo.save(transaction);

            generalSettings.getSimphonyQuota().setUsedTransactionQuota(generalSettings.getSimphonyQuota().getUsedTransactionQuota() + 1);
            generalSettingsRepo.save(generalSettings);

            response.put("isSuccess", true);
            response.put("rest", conversions.roundUpDouble(rest));
            response.put("newBalance", conversions.roundUpDouble(newBalance));
            response.put("paidAmount", conversions.roundUpDouble(paidAmount));
            response.put("discountId", group.getSimphonyDiscount().getDiscountId());
            response.put("group", group.getName());
            response.put("user", user.getName());

            return response;

        }catch(Exception e){
            e.printStackTrace();

            response.put("isSuccess", false);
            response.put("message", "This is an error occurs, Please contact support team.");
            return response;
        }
    }

    public HashMap checkForDiscount(Transactions transaction, Account account,
                                    GeneralSettings generalSettings, RevenueCenter revenueCenter){
        HashMap response = new HashMap();
        response.put("isSuccess", false);
        response.put("includeDiscount", false);

        try {
            ApplicationUser user = userRepo.findByCodeAndAccountIdAndDeleted(transaction.getCode(), account.getId(), false);
            transaction.setAccountId(account.getId());

            if (user == null) {
                response.put("message", "No user for this code.");
            } else if (user.isDeleted()) {
                response.put("message", "This user is deleted.");
            } else if (!user.getAccountId().equals(account.getId())) {
                response.put("message", "This user does not belong to this account wallet system.");
            } else if (transactionRepo.existsByCheckNumberAndRevenueCentreIdAndStatusAndAccountId(
                    transaction.getCheckNumber(), transaction.getRevenueCentreId(), Constants.PAID_TRANSACTION, account.getId())) {
                response.put("message", "Can't use code for the same check twice.");
            } else {
                Optional<Group> groupOptional = groupRepo.findById(user.getGroup().getId());
                if (!groupOptional.isPresent()) {
                    response.put("message", "No group for this user.");
                } else if (groupOptional.get().isDeleted()) {
                    response.put("message", "This group is deleted.");
                } else{
                    Group group = groupOptional.get();

                    if(group.getSimphonyDiscount().getDiscountId() != 0){
                        // Check balance before applying discount
                        double amount = transaction.getTotalPayment();

                        if(!group.getCanteenConfiguration().isIncludeFees()){
                            amount = transaction.getNetAmount();
                        }

                        /* check if account has loyalty feature */
                        if(group.getSimphonyDiscount().getDiscountId() != 0){
                            ArrayList<SimphonyDiscount> discounts = generalSettings.getDiscountRates();
                            SimphonyDiscount simphonyDiscount =
                                    conversions.checkSimphonyDiscountExistence(discounts, group.getSimphonyDiscount().getDiscountId());

                            double netAmount = transaction.getNetAmount();
                            double discount = simphonyDiscount.getDiscountRate();
                            double amountAfterDiscount = netAmount - Math.round(netAmount * (discount / 100));

                            if(!group.getCanteenConfiguration().isIncludeFees()){
                                transaction.setAfterDiscount(amountAfterDiscount);
                            }else {
                                // Calculate service charge and vat after discount
                                double newServiceCharges = Math.round((transaction.getServiceCharges() * amountAfterDiscount)/netAmount);
                                double newVat = Math.round((transaction.getVat() * amountAfterDiscount)/netAmount);
                                transaction.setAfterDiscount(amountAfterDiscount +
                                        newServiceCharges + newVat);
                            }

                        }else {
                            transaction.setAfterDiscount(amount);
                        }

                        Wallet wallet = user.getWallet();
                        double rest = transaction.getAfterDiscount();

                        for(int i = 0; i <  wallet.getBalance().size(); i++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;

                            if(conversions.containRevenueCenter(wallet.getBalance().get(i), revenueCenter)){
                                if(wallet.getBalance().get(i).getAmount() >= rest) {
                                    rest = 0;
                                    break;
                                }else{
                                    rest -= wallet.getBalance().get(i).getAmount();
                                    wallet.getBalance().get(i).setAmount(0);
                                    continue;
                                }
                            }
                        }

                        if(rest == transaction.getAfterDiscount()){
                            response.put("rest", conversions.roundUpDouble(transaction.getAfterDiscount()));
                            response.put("message", user.getName() + " has no balance to spend at this revenue center.");
                        }else{
                            response.put("isSuccess", true);
                            response.put("includeDiscount", true);
                            response.put("discountId", group.getSimphonyDiscount().getDiscountId());
                        }
                    }else {
                        response.put("isSuccess", true);
                        response.put("includeDiscount", false);
                        response.put("discountId", 0);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();

            response.put("discountId", 0);
            response.put("message", "Something went wrong, Please contact support team.");
        }

        return response;
    }

    public double calculateBalance(Wallet wallet){
        double balance = 0;
        List<Balance> balances = wallet.getBalance();
        for(Balance tempBalance : balances){
            balance += tempBalance.getAmount();
        }
        return balance;
    }
}

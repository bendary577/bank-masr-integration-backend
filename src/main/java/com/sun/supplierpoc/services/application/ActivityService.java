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

        try {
            ApplicationUser user = userRepo.findByCodeAndAccountIdAndDeleted(transaction.getCode(), account.getId(), false);

            if(user == null){
                response.put("message", "No user for this code.");
            }else if(user.isDeleted()){
                response.put("message", "This user is deleted.");
            } else if(!user.getAccountId().equals(account.getId())){
                response.put("message", "This user does not belong to this account loyalty program.");
            } else if(transactionRepo.existsByCheckNumberAndRevenueCentreIdAndStatus(
                    transaction.getCheckNumber(), transaction.getRevenueCentreId(), Constants.PAID_TRANSACTION)){
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
                        transaction.setAfterDiscount(transaction.getTotalPayment());
                        transaction.setPointsRedeemed(points);

                        user.setPoints(user.getPoints() - points);
                    }
                    else if(transactionType.getName().equals(Constants.REWARD_POINTS)){
                        int points = (int) Math.round((transaction.getTotalPayment() * generalSettings.getPointReward())/100);

                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(transaction.getTotalPayment());
                        transaction.setPointsRedeemed(0);
                        transaction.setPointsReward(points);
                        user.setPoints(user.getPoints() + points);
                    }
                    else if(!transactionType.getName().equals(Constants.USE_WALLET)) {

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
                        transaction.setAfterDiscount(amountAfterDiscount);
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
                        if(user.isExpired() || user.getExpire() == 0 || user.isSuspended() || user.isDeleted()){
                            response.put("isSuccess", false);
                            response.put("message", "This user is currently unable to use the wallet system.");
                            return response;
                        }

                        /* check if account has loyalty system */
                        transaction.setAfterDiscount(transaction.getTotalPayment());

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
                        transaction.setPartialPayment(paidAmount);

                        /* Remove zero balance/voucher */
                        List<Balance> newBalanceList = new ArrayList<>();
                        for(int i = 0; i <  wallet.getBalance().size(); i ++){
                            if(wallet.getBalance().get(i).getAmount() == 0)
                                continue;
                            else
                                newBalanceList.add(wallet.getBalance().get(i));
                        }
                        wallet.setBalance(newBalanceList);

                        WalletHistory walletHistory = new WalletHistory("Use wallet in " + revenueCenter.getRevenueCenter(),
                                paidAmount, previousBalance, calculateBalance(wallet), null, new Date());
                        wallet.getWalletHistory().add(walletHistory);
                        user.setWallet(wallet);

                        if(rest == transaction.getAfterDiscount()){
                            response.put("rest", transaction.getAfterDiscount());
                            transaction.setStatus(Constants.INSUFFICIENT_AMOUNT);
                            response.put("message", "Guest has no balance to spend at this revenue center.");
                        }else if(rest != 0){
                            transaction.setStatus(Constants.PARTIAL_PAYMENT);
                            response.put("message", "Payment added succefully.");
                        }else if(rest == 0){
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                            response.put("message", "Check paid successfully.");
                        }else {
                            transaction.setStatus(Constants.PAID_TRANSACTION);
                        }

                        response.put("rest", rest);
                        response.put("newBalance", newBalance);
                        response.put("paidAmount", paidAmount);
                    }
                    else{
                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(transaction.getTotalPayment());
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

    public double calculateBalance(Wallet wallet){
        double balance = 0;
        List<Balance> balances = wallet.getBalance();
        for(Balance tempBalance : balances){
            balance += tempBalance.getAmount();
        }
        return balance;
    }
}

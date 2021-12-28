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
            ApplicationUser user = userRepo.findByCode(transaction.getCode());
            if(user == null){
                response.put("message", "No user for this code.");
            }else if(user.isDeleted()){
                response.put("message", "This user is deleted.");
            } else if(!user.getAccountId().equals(account.getId())){
                response.put("message", "This user does not belong to this account loyalty program.");
            } else if(transactionRepo.existsByCheckNumberAndRevenueCentreId(transaction.getCheckNumber(), transaction.getRevenueCentreId())){
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

//                    private float pointReward = 0; // percentage
//                    private float pointsRedemption = 0; // 1$ = ? points

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
                        response.put("message", "Discount added successfully.");
                    }
                    else{
                        transaction.setDiscountRate(0.0);
                        transaction.setAfterDiscount(transaction.getTotalPayment());
                    }

                    if(user.isGeneric() && user.getWallet() != null){
                        Wallet wallet = user.getWallet();
                        double previousBalance = calculateBalance(wallet);
                        double rest = transaction.getAfterDiscount();
                        for(int i = 0; i <  wallet.getBalance().size(); i ++){
                            if(conversions.containRevenueCenter(wallet.getBalance().get(i), revenueCenter)){
                                if(wallet.getBalance().get(i).getAmount() >= transaction.getAfterDiscount()) {
                                    double newBalance = wallet.getBalance().get(i).getAmount() - transaction.getAfterDiscount();
                                    wallet.getBalance().get(i).setAmount(newBalance);
                                    rest = 0;
                                }else{
                                    rest -= wallet.getBalance().get(i).getAmount();
                                    wallet.getBalance().get(i).setAmount(0);
                                    if((i+1) < wallet.getBalance().size()) {
                                        continue;
                                    }
                                }
                                WalletHistory walletHistory = new WalletHistory("Use wallet in " + revenueCenter.getRevenueCenter(),
                                        amount, previousBalance, calculateBalance(wallet), null, new Date());
                                wallet.getWalletHistory().add(walletHistory);
                                user.setWallet(wallet);
                                break;
                            }
                        }
                        if(rest == transaction.getAfterDiscount()){
                            response.put("rest", transaction.getAfterDiscount());
                            if(conversions.hasBalance(wallet.getBalance())){
                                response.put("message", "There is no balance for this revenuecenter.");
                            }else{
                                response.put("message", "Guest has no balance.");
                            }
                        }else if(rest != 0){
                            response.put("message", "Payment added succefully.");
                        }else if(rest == 0){
                            response.put("message", "Check paid successfully.");
                        }
                        response.put("rest", rest);

                    }

                    userRepo.save(user);
                    groupRepo.save(group);

                    transactionRepo.save(transaction);

                    generalSettings.getSimphonyQuota().setUsedTransactionQuota(generalSettings.getSimphonyQuota().getUsedTransactionQuota() + 1);
                    generalSettingsRepo.save(generalSettings);

                    response.put("isSuccess", true);
                    if(transactionType.getName().equals(Constants.REWARD_POINTS)
                            || transactionType.getName().equals(Constants.POINTS_REDEMPTION)){
                        response.put("message", "New balance = " + user.getPoints());
                    }

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

    public double calculateBalance(Wallet wallet){
        double balance = 0;
        List<Balance> balances = wallet.getBalance();
        for(Balance tempBalance : balances){
            balance += tempBalance.getAmount();
        }
        return balance;
    }
}

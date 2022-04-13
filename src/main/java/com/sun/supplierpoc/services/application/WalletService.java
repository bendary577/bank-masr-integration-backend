package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.services.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WalletService {

    @Autowired
    private ApplicationUserRepo applicationUserRepo;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ActionStatsService actionStatsService;

    @Autowired
    private AppUserService appUserService;

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response chargeWallet(User agent, String userId, Balance balance) {

        Response response = new Response();

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepo.findById(userId);
        if(applicationUserOptional.isPresent()){

            ApplicationUser applicationUser = applicationUserOptional.get();

            try{
                double lastBalance = 0;
                for(Balance tempBalance : applicationUser.getWallet().getBalance()){
                    lastBalance = lastBalance+ tempBalance.getAmount();
                }
                applicationUser.getWallet().getBalance().add(balance);
                WalletHistory walletHistory = new WalletHistory(ActionType.CHARGE_WALLET , balance.getAmount() ,
                        lastBalance, (lastBalance + balance.getAmount()), agent, new Date());
                walletHistory.setActionId(UUID.randomUUID().toString());
                applicationUser.getWallet().getWalletHistory().add(walletHistory);
                applicationUserRepo.save(applicationUser);

                /* Create new user action */
                Action action = new Action();
                action.setUser(agent);
                action.setApplicationUser(applicationUser);
                action.setAccountId(agent.getAccountId());
                action.setAmount(balance.getAmount());
                action.setDate(new Date());
                action.setActionType(ActionType.CHARGE_WALLET);

                actionService.createUserAction(action);

                /* Update agent action stats */
                ActionStats actionStats = actionStatsService.findActionStatsByAgent(agent);
                if(actionStats == null){
                    actionStats = new ActionStats(agent, balance.getAmount(), 0,0, agent.getAccountId());
                    actionStatsService.createActionStats(actionStats);
                }else {
                    actionStats.setChargeAmount(actionStats.getChargeAmount() + balance.getAmount());
                    actionStatsService.createActionStats(actionStats);
                }

                response.setStatus(true);
                response.setData(applicationUser);
                response.setMessage("Wallet Charged Successfully.");
            }catch(Exception e) {
                e.printStackTrace();
                response.setStatus(false);
                response.setMessage(e.getMessage());
            }
        }else{
            response.setStatus(false);
            response.setMessage("This guest doesn't exist.");
        }
        return response;
    }

    public Response deductWallet(User agent, String userId, double amount) {

        Response response = new Response();

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepo.findById(userId);
        if(applicationUserOptional.isPresent()){
            ApplicationUser applicationUser = applicationUserOptional.get();
            try {

                double biggerBalance = 0;
                double lastBalance = 0;
                int index = 0;

                for (int i = 0; i < applicationUser.getWallet().getBalance().size(); i++) {
                    if (applicationUser.getWallet().getBalance().get(i).getAmount() > biggerBalance) {
                        biggerBalance = applicationUser.getWallet().getBalance().get(i).getAmount();
                        index = i;
                    }
                    lastBalance = lastBalance + applicationUser.getWallet().getBalance().get(i).getAmount();
                }

                if (biggerBalance < amount) {
                    response.setBadStatus(false, "There isn't enough balance. ");
                    return response;
                }

                applicationUser.getWallet().getBalance().get(index).setAmount(
                        applicationUser.getWallet().getBalance().get(index).getAmount() - amount
                );

                WalletHistory walletHistory = new WalletHistory(ActionType.DEDUCT_WALLET, amount, lastBalance, (lastBalance - amount),
                        agent, new Date());
                walletHistory.setActionId(UUID.randomUUID().toString());
                applicationUser.getWallet().getWalletHistory().add(walletHistory);
                applicationUserRepo.save(applicationUser);

                /* Create new user action */
                Action action = new Action();
                action.setUser(agent);
                action.setApplicationUser(applicationUser);
                action.setAccountId(agent.getAccountId());
                action.setAmount(amount);
                action.setDate(new Date());
                action.setActionType(ActionType.DEDUCT_WALLET);

                actionService.createUserAction(action);

                /* Update agent action stats */
                ActionStats actionStats = actionStatsService.findActionStatsByAgent(agent);
                if(actionStats == null){
                    actionStats = new ActionStats(agent, 0, amount,0, agent.getAccountId());
                    actionStatsService.createActionStats(actionStats);
                }else {
                    actionStats.setDeductAmount(actionStats.getDeductAmount() + amount);
                    actionStatsService.createActionStats(actionStats);
                }

                response.setMessage("Amount deducted from Wallet successfully.");
                response.setStatus(true);
                response.setData(applicationUser);
                return response;
            }catch(Exception e) {
                response.setMessage(e.getMessage());
            }
        }else{
            response.setMessage("This guest doesn't exist.");
        }
        response.setStatus(false);
        return response;
    }

    public Response undoWalletAction(User agent, String userId, String actionId) {

        Response response = new Response();

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepo.findById(userId);
        if(applicationUserOptional.isPresent()){

            ApplicationUser applicationUser = applicationUserOptional.get();

            try{
                double lastBalance = 0;
                for(Balance tempBalance : applicationUser.getWallet().getBalance()){
                    lastBalance = lastBalance+ tempBalance.getAmount();
                }
                WalletHistory walletHistoryChosen = null;
                for(WalletHistory walletHistory : applicationUser.getWallet().getWalletHistory()){
                    if(walletHistory.getActionId().equals(actionId)){
                        walletHistoryChosen = walletHistory;
                        break;
                    }
                }

                if(walletHistoryChosen != null){

                    if(walletHistoryChosen.isDeleted()){
                        response.setStatus(false);
                        response.setMessage("Wallet action is already deleted");
                        return response;
                    }

                    Balance balance = new Balance();
                    double newBalanceAmount = 0;
                    walletHistoryChosen.setPreviousBalance(lastBalance);
                    if(walletHistoryChosen.getOperation().equals(ActionType.CHARGE_WALLET) || walletHistoryChosen.getOperation().equals(ActionType.ENTRANCE_AMOUNT)){
                         newBalanceAmount = walletHistoryChosen.getAmount() * -1;
//                        walletHistoryChosen.setNewBalance(lastBalance-walletHistoryChosen.getAmount());
                    }else if(walletHistoryChosen.getOperation().equals(ActionType.DEDUCT_WALLET)){
                        newBalanceAmount = walletHistoryChosen.getAmount();
//                        walletHistoryChosen.setNewBalance(lastBalance+walletHistoryChosen.getAmount());
                    }

                    walletHistoryChosen.setDeleted(true);
                    balance.setAmount(newBalanceAmount);
                    applicationUser.getWallet().getBalance().add(balance);
                    applicationUserRepo.save(applicationUser);

                    /* Create new user action */
                    Action action = new Action();
                    action.setUser(agent);
                    action.setApplicationUser(applicationUser);
                    action.setAccountId(agent.getAccountId());
                    action.setDate(new Date());
                    action.setAmount(0);
                    action.setActionType(ActionType.UNDO_WALLET_ACTION);

                    actionService.createUserAction(action);

                    /* Update agent action stats */
                    ActionStats actionStats = actionStatsService.findActionStatsByAgent(agent);
                    if(actionStats == null){
                        actionStats = new ActionStats(agent, 0, 0,0, agent.getAccountId());
                        actionStatsService.createActionStats(actionStats);
                    }else {
                        actionStats.setChargeAmount(actionStats.getChargeAmount() + 0);
                        actionStatsService.createActionStats(actionStats);
                    }

                    response.setStatus(true);
                    response.setData(applicationUser);
                    response.setMessage("Wallet Action Reverted Successfully.");
                    return response;
                }else{
                    response.setStatus(false);
                    response.setMessage("Wallet Action cannot be found");
                }
            }catch(Exception e) {
                e.printStackTrace();
                response.setStatus(false);
                response.setMessage(e.getMessage());
            }
        }else{
            response.setStatus(false);
            response.setMessage("This guest doesn't exist.");
        }
        return response;
    }

    public Response getWalletsRemainingTotal(Account account, String fromDate, String toDate) {

        Response response = new Response();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        //get app users
        ArrayList<ApplicationUser> applicationUsers = appUserService.getAppUsersByAccountId(account.getId());

        // total remaining
        double totalRemaining = 0;

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            if(fromDate.equals("")){
                fromDate = dateFormat.format(new Date());
            }
            if(toDate.equals("")){
                toDate = dateFormat.format(new Date());
            }
            Date start = dateFormat.parse(fromDate);
            Date end =  dateFormat.parse(toDate);

            //loop on each user wallet
            for (ApplicationUser applicationUser: applicationUsers) {
                double totalWalletHistory = 0;

                //get latest wallet history
                WalletHistory latestWalletHistory;

                //check if action is not deleted
                int counter = applicationUser.getWallet().getWalletHistory().size()-1; //latest action index
                do{
                    latestWalletHistory = applicationUser.getWallet().getWalletHistory().get(counter);
                    counter--;
                }while(latestWalletHistory.isDeleted());
                String latestWalletHistoryDateString = dateFormat.format(latestWalletHistory.getDate());
                Date latestWalletHistoryDate = dateFormat.parse(latestWalletHistoryDateString);

                //if start date is after latest history
                if(start.compareTo(latestWalletHistoryDate) > 0){
                    // remaining of this user = sum of all histories before
                    totalWalletHistory += latestWalletHistory.getNewBalance();
                }else{
                    //create ranged history array
                    List<WalletHistory> rangedWalletHistory = new ArrayList<>();
                    for (WalletHistory walletHistory: applicationUser.getWallet().getWalletHistory()) {
                        if(walletHistory.isDeleted()){
                            continue;
                        }
                        String walletHistoryDateString = dateFormat.format(walletHistory.getDate());
                        Date walletHistoryDate = dateFormat.parse(walletHistoryDateString);

                        if (walletHistoryDate.compareTo(start) < 0) {
                            continue;
                        } else if (walletHistoryDate.compareTo(end) > 0) {
                            continue;
                        }
                        //add to array
                        rangedWalletHistory.add(walletHistory);
                    }
                    if(!rangedWalletHistory.isEmpty()){
                        //get latest
                        totalWalletHistory += rangedWalletHistory.get(rangedWalletHistory.size()-1).getNewBalance();
                    }
                }

                totalRemaining += totalWalletHistory;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception");
        }

        response.setStatus(true);
        response.setMessage("users wallets remaining total returned successfully");
        response.setData(Double.parseDouble(decimalFormat.format(totalRemaining)));

        return response;
    }

//    private double getWalletRemainingTotalFrom(){}
//
//    private double getWalletRemainingTotalTo(){}
//
//    private double getWalletRemainingTotalFromTo(){}
//
//    private double getAllWalletRemainingTotal(){}


}

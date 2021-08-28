package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Balance;
import com.sun.supplierpoc.models.applications.Wallet;
import com.sun.supplierpoc.models.applications.WalletHistory;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private ApplicationUserRepo applicationUserRepo;

    public Response chargeWallet(String userId, Balance balance) {

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
                WalletHistory walletHistory = new WalletHistory("Charge Wallet" , balance.getAmount() , lastBalance, (lastBalance + balance.getAmount()), new Date());
                applicationUser.getWallet().getWalletHistory().add(walletHistory);
                applicationUserRepo.save(applicationUser);
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


    public Response deductWallet(String userId, double amount) {

        Response response = new Response();

        Optional<ApplicationUser> applicationUserOptional = applicationUserRepo.findById(userId);
        if(applicationUserOptional.isPresent()){
            ApplicationUser applicationUser = applicationUserOptional.get();
            try{
//                Wallet wallet = applicationUser.getWallet();
//                double balance = wallet.getBalance();
//                double newBalance = balance - amount ;
//                wallet.setBalance(newBalance);
//                WalletHistory walletHistory = new WalletHistory("Deduct From Wallet" , amount , balance, newBalance);
//                wallet.getWalletHistory().add(walletHistory);
//                applicationUser.setWallet(wallet);
//                applicationUserRepo.save(applicationUser);
//                response.setStatus(true);
//                response.setData(applicationUser);
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
}

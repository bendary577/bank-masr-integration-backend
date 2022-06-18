package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.SimphonyQuota;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.application.ActivityService;
import com.sun.supplierpoc.services.simphony.SimphonyPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    @Autowired
    AccountService accountService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private SimphonyPaymentService simphonyPaymentService;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/createTransactionActivity")
    @CrossOrigin("*")
    public ResponseEntity<?> transactionActivity(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody Transactions transaction, BindingResult result){

        String message = "You don't have role to use " + transaction.getTransactionTypeName() + " feature!.";
        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("isSuccess", false);
                response.put("message", result.getAllErrors().get(0).getDefaultMessage());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
            Account account = accountService.getAccount(invokerUser.getAccountId());

            if (account != null) {

                if(transaction.getCheckNumber().equals("")){
                    response.put("isSuccess", false);
                    response.put("message", "Check number is a required field.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                TransactionType transactionType;
                if(transaction.getTransactionTypeName().equals("")) {
                    transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());
                }else{
                    transactionType = transactionTypeRepo.findByNameAndAccountId(transaction.getTransactionTypeName(), account.getId());
                }

                if(transactionType == null){
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                if (!invokerUser.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                if(generalSettings.getSimphonyQuota() == null){
                    generalSettings.setSimphonyQuota(new SimphonyQuota());
                    generalSettingsRepo.save(generalSettings);
                }

                if(generalSettings.getSimphonyQuota().getTransactionQuota() == generalSettings.getSimphonyQuota().getUsedTransactionQuota()){
                    response.put("isSuccess", false);
                    response.put("message", "You have exhausted your package of transactions, Pleas contact your service provider.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();

                if (conversions.validateRevenueCenter(revenueCenters, transaction.getRevenueCentreId())) {

                    RevenueCenter revenueCenter = conversions.getRevenueCenter(revenueCenters, transaction.getRevenueCentreId());

                    response = activityService.createTransaction(transactionType, transaction, account, generalSettings
                    , revenueCenter);

                    if ((boolean) response.get("isSuccess")) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                } else {
                    response.put("isSuccess", false);
                    response.put("message", Constants.WRONG_REVENUE_CENTER);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response.put("isSuccess", false);
                response.put("message", Constants.INVALID_USER);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("isSuccess", false);
            response.put("message", "Some thing went wrong.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    //////////////////////////////////////////// Canteen ///////////////////////////////////////////////////////////////

    @RequestMapping("/checkForDiscount")
    @CrossOrigin("*")
    public ResponseEntity<?> checkForDiscount(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody Transactions transaction, BindingResult result){

        String message = "You don't have role to use " + transaction.getTransactionTypeName() + " feature!.";
        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("isSuccess", false);
                response.put("message", result.getAllErrors().get(0).getDefaultMessage());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
            Account account = accountService.getAccount(invokerUser.getAccountId());

            if (account != null) {
                TransactionType transactionType =
                        transactionTypeRepo.findByNameAndAccountId(transaction.getTransactionTypeName(), account.getId());

                if(transactionType == null){
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                if (!invokerUser.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                if(generalSettings.getSimphonyQuota() == null){
                    generalSettings.setSimphonyQuota(new SimphonyQuota());
                    generalSettingsRepo.save(generalSettings);
                }

                if(generalSettings.getSimphonyQuota().getTransactionQuota() == generalSettings.getSimphonyQuota().getUsedTransactionQuota()){
                    response.put("isSuccess", false);
                    response.put("message", "You have exhausted your package of transactions, Pleas contact your service provider.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();

                if (conversions.validateRevenueCenter(revenueCenters, transaction.getRevenueCentreId())) {

                    RevenueCenter revenueCenter = conversions.getRevenueCenter(revenueCenters, transaction.getRevenueCentreId());

                    response = activityService.checkForDiscount(transaction, account, generalSettings, revenueCenter);

                    if ((boolean) response.get("isSuccess")) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                } else {
                    response.put("isSuccess", false);
                    response.put("message", Constants.WRONG_REVENUE_CENTER);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response.put("isSuccess", false);
                response.put("message", Constants.INVALID_USER);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("isSuccess", false);
            response.put("message", "Some thing went wrong.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @RequestMapping("/createCanteenTransaction")
    @CrossOrigin("*")
    public ResponseEntity<?> createCanteenTransaction(@RequestHeader("Authorization") String authorization,
                                              @Valid @RequestBody Transactions transaction, BindingResult result){

        String message = "You don't have role to use " + transaction.getTransactionTypeName() + " feature!.";
        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("isSuccess", false);
                response.put("message", result.getAllErrors().get(0).getDefaultMessage());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
            Account account = accountService.getAccount(invokerUser.getAccountId());

            if (account != null) {
                TransactionType transactionType =
                        transactionTypeRepo.findByNameAndAccountId(transaction.getTransactionTypeName(), account.getId());

                if(transactionType == null){
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                if (!invokerUser.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", false);
                    response.put("message", message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                if(generalSettings.getSimphonyQuota() == null){
                    generalSettings.setSimphonyQuota(new SimphonyQuota());
                    generalSettingsRepo.save(generalSettings);
                }

                if(generalSettings.getSimphonyQuota().getTransactionQuota() == generalSettings.getSimphonyQuota().getUsedTransactionQuota()){
                    response.put("isSuccess", false);
                    response.put("message", "You have exhausted your package of transactions, Pleas contact your service provider.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();

                if (conversions.validateRevenueCenter(revenueCenters, transaction.getRevenueCentreId())) {

                    RevenueCenter revenueCenter = conversions.getRevenueCenter(revenueCenters, transaction.getRevenueCentreId());

                    response = activityService.createCanteenTransaction(transactionType,transaction, account,
                            generalSettings, revenueCenter);

                    if ((boolean) response.get("isSuccess")) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                } else {
                    response.put("isSuccess", false);
                    response.put("message", Constants.WRONG_REVENUE_CENTER);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response.put("isSuccess", false);
                response.put("message", Constants.INVALID_USER);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("isSuccess", false);
            response.put("message", "Some thing went wrong.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }





    //------------------------------------------ payment transaction ----------------------------------------
    private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(100_000);
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(100_000);
        return clientHttpRequestFactory;
    }
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    @PostMapping("/activity/payCheck")
    public ResponseEntity<?> paymentTransaction(@RequestHeader("Authorization") String authorization,
                                                @Valid @RequestBody SimphonyPaymentReq simphonyPayment,
                                                BindingResult result) {
        Response response = new Response();

        try{

            ResponseEntity paxResult = restTemplate.getForEntity("http://41.64.174.230:4040" + "?transactionAmount=" +
                Math.round(1f) + "&currency=" + "eg" + "&transType=" + "12", String.class);

            paxResult.getBody();

            if(!result.hasErrors()) {

                InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

                if(invokerUser != null) {


                    Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());

                    if (accountOptional.isPresent()) {

                        Account account = accountOptional.get();
                        response = simphonyPaymentService.createSimphonyPaymentTransaction(simphonyPayment, account);

                    } else {
                        response.setStatus(false);
                        response.setMessage(Constants.ACCOUNT_NOT_EXIST);
                    }
                }else{

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_USER);

                }

            }else{

                response.setStatus(false);
                response.setMessage(result.getAllErrors().get(0).getDefaultMessage());

            }
        }catch(Exception e){
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

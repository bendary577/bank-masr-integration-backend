package com.sun.supplierpoc.controllers.application;


import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.SimphonyQuota;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.models.voucher.VoucherTransaction;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.voucher.VoucherTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/simphonyLoyalty/voucherTransactions")
public class VoucherTransController {

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    @Autowired
    private Conversions conversions;

    @Autowired
    private VoucherTransService voucherTransService;

    @RequestMapping("/createVoucherTrans")
    @CrossOrigin("*")
    public ResponseEntity<?> transactionActivity(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody Transactions voucherTransaction, BindingResult result){

        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("isSuccess", false);
                response.put("message", result.getAllErrors().get(0).getDefaultMessage());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

            if(invokerUser != null) {
                Account account = accountService.getAccount(invokerUser.getAccountId());

                if (account != null) {

                    if (voucherTransaction.getCheckNumber().equals("")) {
                        response.put("isSuccess", false);
                        response.put("message", "Check number is a required field.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                    TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());
                    if (!invokerUser.getTypeId().contains(transactionType.getId())) {
                        response.put("isSuccess", false);
                        response.put("message", "You don't have role to redeem reward!.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                    if (generalSettings.getSimphonyQuota() == null) {
                        generalSettings.setSimphonyQuota(new SimphonyQuota());
                        generalSettingsRepo.save(generalSettings);
                    }

                    if (generalSettings.getSimphonyQuota().getTransactionQuota() == generalSettings.getSimphonyQuota().getUsedTransactionQuota()) {
                        response.put("isSuccess", false);
                        response.put("message", "You have exhausted your package of transactions, Pleas contact your service provider.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                    ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();

                    if (conversions.validateRevenueCenter(revenueCenters, voucherTransaction.getRevenueCentreId())) {

                        RevenueCenter revenueCenter = conversions.getRevenueCenter(revenueCenters, voucherTransaction.getRevenueCentreId());

                        response = voucherTransService.createTransaction(transactionType, voucherTransaction, account, generalSettings, revenueCenter);

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
            }else{
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

    @RequestMapping("/getTotalVoucherTrans")
    public ResponseEntity getTotalVoucherTrans(Principal principal,
                                               @RequestParam("voucherId") String voucherId,
                                               @RequestParam("voucherCode") String voucherCode,
                                               @RequestParam("page") int page,
                                               @RequestParam("size") int size) {

        HashMap response = new HashMap();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = voucherTransService.getTotalVoucherTransactions(page, size, voucherId, voucherCode, account);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    @RequestMapping("/getVoucherTransStatistics")
    public ResponseEntity getVoucherTransStatistics(Principal principal,
                                                    @RequestParam("voucherCode") String voucherCode,
                                                    @RequestParam("voucherId") String voucherId) {

        HashMap response = new HashMap();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = voucherTransService.getVoucherTransStatistics(voucherId, voucherCode, account);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }
}

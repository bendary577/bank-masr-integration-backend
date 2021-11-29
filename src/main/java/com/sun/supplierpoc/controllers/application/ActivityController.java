package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.SimphonyQuota;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.application.ActivityService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/createTransactionActivity")
    @CrossOrigin("*")
    public ResponseEntity<?> transactionActivity(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody Transactions transaction, BindingResult result,
                                                 @RequestParam(name = "payWithPoints", required = false) int payWithPoints) {

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
                if(payWithPoints == 1){
                    transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.POINTS_REDEMPTION, account.getId());
                }
                else if(payWithPoints == 2){
                    transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REWARD_POINTS, account.getId());
                }
                else if(transaction.getTransactionTypeId().equals("")) {
                    transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());
                }else{
                    transactionType = transactionTypeRepo.findByIdAndAccountId(transaction.getTransactionTypeId(), account.getId());
                }

                if (!invokerUser.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", false);
                    response.put("message", "You don't have role to redeem reward!.");
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

}

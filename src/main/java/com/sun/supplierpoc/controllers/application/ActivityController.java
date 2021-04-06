package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.application.ActivityService;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    @Autowired
    AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    private final Conversions conversions = new Conversions();

    @RequestMapping("/createTransactionActivity")
    @CrossOrigin("*")
    public ResponseEntity<?> transactionActivity(@RequestHeader("Authorization") String authorization,
                                                 @Valid @RequestBody Transactions transaction, BindingResult result) {

        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("error", result.getAllErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            InvokerUser user = invokerUserService.getAuthenticatedUser(authorization);
            Account account = accountService.getAccount(user.getAccountId());

            if (account != null) {
                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
//                SimphonyLocation location = generalSettings.getSimphonyLocationsByID(transaction.getRevenueCentreId());

                //Constants.REDEEM_VOUCHER)

                TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());

                if (!user.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", Constants.FAILED);
                    response.put("message", "You don't have role to redeem reward!.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
//                location.isChecked()
                if (true) {
                    response = activityService.createTransaction(transactionType, transaction);

                    if (response.get("isSuccess").equals(Constants.SUCCESS)) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }

                } else {
                    response.put("error", Constants.WRONG_REVENUE_CENTER);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

            } else {
                response.put("error", Constants.INVALID_USER);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.application.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

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
                response.put("isSuccess", false);
                response.put("message", result.getAllErrors().get(0).getDefaultMessage());
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            InvokerUser user = invokerUserService.getAuthenticatedUser(authorization);
            Account account = accountService.getAccount(user.getAccountId());

            if (account != null) {
                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                //SimphonyLocation location = generalSettings.getSimphonyLocationsByID(transaction.getRevenueCentreId());
                //Constants.REDEEM_VOUCHER)

                TransactionType transactionType = transactionTypeRepo.findByNameAndAccountId(Constants.REDEEM_VOUCHER, account.getId());

                if (!user.getTypeId().contains(transactionType.getId())) {
                    response.put("isSuccess", false);
                    response.put("message", "You don't have role to redeem reward!.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }
//                location.isChecked()
                if (true) {
                    response = activityService.createTransaction(transactionType, transaction);

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

            response.put("isSuccess", false);
            response.put("message", "Some thing went wrong.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

}

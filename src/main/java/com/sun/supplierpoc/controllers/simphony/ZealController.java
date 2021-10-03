package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.Message;
import com.sun.supplierpoc.models.simphony.response.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.response.ZealRedeemResponse;
import com.sun.supplierpoc.models.simphony.check.ZealPayment;
import com.sun.supplierpoc.models.simphony.check.ZealVoucher;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import com.sun.supplierpoc.services.simphony.ZealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/zeal")
public class ZealController {
    @Autowired
    private ZealService zealService;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    MenuItemService menuItemService;
    @Autowired
    SyncJobService syncJobService;
    @Autowired
    SyncJobDataService syncJobDataService;
    @Autowired
    AccountService accountService;
    @Autowired
    InvokerUserService invokerUserService;

    @Autowired
    private OperationTypeRepo operationTypeRepo;

    private Conversions conversions = new Conversions();

    @PostMapping("/zealLoyalty")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<?> zealPayment(@RequestHeader("Authorization") String authorization,
                                         @RequestBody ZealPayment zealPayment) {

        ZealLoyaltyResponse response = new ZealLoyaltyResponse();

        String username, password;
        try {
            final String[] values = conversions.convertBasicAuth(authorization);
            if (values.length != 0) {

                username = values[0];
                password = values[1];
                InvokerUser user = invokerUserService.getInvokerUser(username, password);

                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                    SimphonyLocation location = generalSettings.getSimphonyLocationsByID(zealPayment.getRevenueCentreId());

                    OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Payment", account.getId(), false);


                    if (!user.getTypeId().contains(operationType.getId())){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have role to add loyalty!.");
                    }


                    if (location.isChecked()) {
                        response = zealService.zealPaymentProcessor(zealPayment, user, account, location);
                        if (!response.isLoyalty()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                    }else{
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Location");
                    }

                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                String message = "Invalid Credentials";
                response.setMessage(message);
                response.setLoyalty(false);

            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/zealRedeem")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<?> zealVoucher(@RequestHeader("Authorization") String authorization,
                                         @RequestBody ZealVoucher zealVoucher) {

        ZealRedeemResponse response = new ZealRedeemResponse();

        String username, password;
        try {
            final String[] values = conversions.convertBasicAuth(authorization);
            if (values.length != 0) {
                username = values[0];
                password = values[1];

                InvokerUser user = invokerUserService.getInvokerUser(username, password);
                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isPresent()) {

                    Account account = accountOptional.get();

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                    SimphonyLocation location = generalSettings.getSimphonyLocationsByID(zealVoucher.getRevenueCentreId());

                    OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Voucher", account.getId(), false);

                    if (!user.getTypeId().contains(operationType.getId())){
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have role to redeem reward!.");
                    }

                    if (location.isChecked()) {
                        response = zealService.zealVoucherProcessor(zealVoucher, user, account, location);
                        if (!response.isStatus()) {
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }
                    Message message = new Message();
                    message.setEn("error message wrong revenue center, please configure this RC");
                    response.setMessage(message);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }
                String message = "Invalid Credentials";
                //response.setMessage(message);
                response.setStatus(false);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
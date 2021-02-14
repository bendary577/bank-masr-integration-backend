package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.ZealRedeemResponse;
import com.sun.supplierpoc.models.simphony.check.ZealPayment;
import com.sun.supplierpoc.models.simphony.check.ZealPoints;
import com.sun.supplierpoc.models.simphony.check.ZealVoucher;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import com.sun.supplierpoc.services.simphony.ZealService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/zeal")
public class ZealController {

    Logger logger = LoggerFactory.getLogger(ZealController.class);

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

                //User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                    SimphonyLocation location = generalSettings.getSimphonyLocationsByID(zealPayment.getRevenueCentreId());

                    if (location.isChecked()) {
                        response = zealService.zealPaymentProcessor(zealPayment, user, account, location);
                        if (!response.isLoyalty()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
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

//                    logger.info(location.toString());
                    if (location.isChecked()) {
                        response = zealService.zealVoucherProcessor(zealVoucher, user, account, location);
                        if (!response.isStatus()) {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

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

    @PostMapping("/zealPoints")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> zealPoints(@RequestBody ZealPoints zealPoints) {
        Response response = new Response();

        String username = "test1";
        String password = "test@test";

        InvokerUser user = invokerUserService.getInvokerUser(username, password);
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            ArrayList<SimphonyLocation> locations = generalSettings.getSimphonyLocations();

            for (SimphonyLocation location : locations) {

                if (location.isChecked()) {

                    response = zealService.zealPointsProcessor(zealPoints, user.getId(), account, location.getRevenueCenterID());

                    if (!response.isStatus()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        String message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

}
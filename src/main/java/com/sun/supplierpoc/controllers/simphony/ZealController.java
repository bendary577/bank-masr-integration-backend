package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
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
    @Autowired
    private OperationTypeRepo operationTypeRepo;
    @Autowired
    private OperationRepo operationRepo;
    @Autowired
    private OperationDataRepo operationDataRepo;

    @PostMapping("/zealPayment")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> osimphonyZealPayment(@RequestBody ZealPayment zealPayment) {

        Response response = new Response();

        String username = "test1";
        String password = "test@test";

        InvokerUser user = invokerUserService.getInvokerUser(username, password);

        //User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            ArrayList<SimphonyLocation> locations = generalSettings.getSimphonyLocations();

            for (SimphonyLocation location : locations) {

                if (location.isChecked()) {

                    response = zealService.zealPaymentService(zealPayment, user.getId(), account, location.getRevenueCenterID());

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


    @PostMapping("/zealVoucher")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> simphonyZealVoucher(@RequestBody ZealVoucher zealVoucher) {
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

                    logger.info("get service");
                    response = zealService.simphonyZealVoucher(zealVoucher, username, user.getId(), account, location.getRevenueCenterID());

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

    @PostMapping("/zealPoints")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> simphonyZealPoints(@RequestBody ZealPoints zealPoints) {
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

                    response = zealService.simphonyZealPoints(zealPoints, user.getId(), account, location.getRevenueCenterID());

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
package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.request.CreateCheckRequest;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OperationRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.CreateOrderService;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.util.*;

@RestController()
@RequestMapping(value = {"/Simphony"})
public class CreateOrder {

    @Autowired
    private OperationTypeRepo operationTypeRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    MenuItemService menuItemService;
    @Autowired
    AccountService accountService;
    @Autowired
    InvokerUserService invokerUserService;
    @Autowired
    private CreateOrderService createOrderService;
    @Autowired
    private OperationRepo operationRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    private Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(path = "/CreateOrder", produces = MediaType.APPLICATION_JSON)
    public ResponseEntity CreateOpenCheckRequest(@Valid @RequestBody CreateCheckRequest checkDetails, BindingResult result
            , @RequestHeader("Authorization") String authorization) {

        if (result.hasErrors()) {
            HashMap<String, Object> errors = new HashMap<>();
            errors.put("Date", LocalDateTime.now());
            result.getAllErrors().forEach(error -> errors.put("error", error.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        int revenueCenterID = Integer.parseInt(checkDetails.getpGuestCheck().revenue());

        String username, password;
        try {
            final String[] values = conversions.convertBasicAuth(authorization);
            if (values.length != 0) {
                username = values[0];
                password = values[1];

                InvokerUser invokerUser = invokerUserService.getInvokerUser(username, password);
                if (invokerUser != null) {
                    Optional<Account> accountOptional = accountService.getAccount(invokerUser.getAccountId());

                    if (accountOptional.isPresent()) {
                        Account account = accountOptional.get();

                        OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted(Constants.CREATE_CHECK, account.getId(), false);

                        if (!invokerUser.getTypeId().contains(operationType.getId())) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                    new HashMap<String, Object>() {{
                                        put("error", "You don't have role to create check!");
                                        put("Date", LocalDateTime.now());
                                    }});
                        }

                        Operation operation = new Operation(Constants.RUNNING, "", new Date(), null, invokerUser.getId(),
                                account.getId(), operationType.getId(), revenueCenterID, false);

                        operationRepo.save(operation);

                        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                        SimphonyLocation location = generalSettings.getSimphonyLocationsByID(revenueCenterID);

                        if (location == null) {

                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                    new HashMap<String, Object>() {{
                                        put("error", "Revenue center not found.");
                                        put("Date", LocalDateTime.now());
                                    }});
                        }

                        ResponseEntity responseEntity = this.menuItemService.PostTransactionEx(checkDetails, location, operationType);

                        if (responseEntity.getStatusCode().isError()) {
                            operation.setStatus(Constants.FAILED);
                            operation.setReason(responseEntity.getBody().toString());
                            operation.setEndDate(new Date());
                            operation.setRowsFetched(0);
//                            createOrderService.saveOrderCreation(checkDetails, operation);
                            operationRepo.save(operation);
                        } else {
                            operation.setStatus(Constants.SUCCESS);
                            operation.setEndDate(new Date());
                            operation.setRowsFetched(1);
//                            createOrderService.saveOrderCreation(checkDetails, operation);
                            operationRepo.save(operation);
                        }
                        return responseEntity;
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                                new HashMap<String, Object>() {{
                                    put("error", "Wrong username or password.");
                                    put("Date", LocalDateTime.now());
                                }});
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new HashMap<String, Object>() {{
                                put("error", "Wrong username or password.");
                                put("Date", LocalDateTime.now());
                            }});
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new HashMap<String, Object>() {{
                            put("error", "Wrong username or password.");
                            put("Date", LocalDateTime.now());
                        }});
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new HashMap<String, Object>() {{
                        put("error", "Something went wrong!.");
                        put("Date", LocalDateTime.now());
                    }});
        }
    }
}

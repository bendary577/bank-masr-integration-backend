package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.OperationType;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Optional;

@RestController()
@RequestMapping(value = {"/Simphony"})
public class CreateOrder {
    @Autowired
    private SyncJobRepo syncJobRepo;
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
    private Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(path ="/CreateOrder",produces= MediaType.APPLICATION_JSON)
    public ResponseEntity CreateOpenCheckRequest(@RequestHeader("Authorization") String authorization,
                                                 @RequestBody PostTransactionEx2 checkDetails) {
        int revenueCenterID = Integer.parseInt(checkDetails.getpGuestCheck().getCheckRevenueCenterID());
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


                        if (!invokerUser.getTypeId().equals(operationType.getId())){
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have role to create check!");
                        }

                        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, invokerUser.getId(),
                                account.getId(), operationType.getId(), 0);

                        syncJobRepo.save(syncJob);

                        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                        SimphonyLocation location = generalSettings.getSimphonyLocationsByID(revenueCenterID);
                        ResponseEntity responseEntity= this.menuItemService.PostTransactionEx(checkDetails, location, operationType);

                        if(responseEntity.getStatusCode().isError()){
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason(responseEntity.getBody().toString());
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(0);

                            syncJobRepo.save(syncJob);
                        }
                        else {
                            syncJob.setStatus(Constants.SUCCESS);
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(1);

                            syncJobRepo.save(syncJob);
                        }

                        return responseEntity;
                    }else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong username or password.");
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong username or password.");
                }
            }else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong username or password.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

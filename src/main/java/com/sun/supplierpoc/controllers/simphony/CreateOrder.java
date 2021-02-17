package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OperationRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.CreateOrderService;
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

    private Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping(path ="/CreateOrder",produces= MediaType.APPLICATION_JSON)
    public ResponseEntity CreateOpenCheckRequest(@RequestHeader("Authorization") String authorization,
                                                 @RequestBody PostTransactionEx2 checkDetails) {

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

                        ///////////////////-/////////////////// Validation /////////////////////////////////////////////
                        if (!invokerUser.getTypeId().equals(operationType.getId()))
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have role to create check!");

                        if (checkDetails.getpGuestCheck() == null)
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Check is a required.");

                        if (checkDetails.getpGuestCheck().revenue() == null || checkDetails.getpGuestCheck().revenue().equals(""))
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Revenue center ID is a required field.");

                        if (checkDetails.getpGuestCheck().getCheckOrderType() == null || checkDetails.getpGuestCheck().getCheckOrderType().equals(""))
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Check order type is a required field.");

                        // ONLINE-ORDER
                        if (checkDetails.getpGuestCheck().getCheckOrderType().equals("1") &&
                                (checkDetails.getpGuestCheck().getPCheckInfoLines() == null
                                        || checkDetails.getpGuestCheck().getPCheckInfoLines().getString() == null
                                        || checkDetails.getpGuestCheck().getPCheckInfoLines().getString().equals("")))
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Address is a required field.");

                        if (checkDetails.getPpMenuItemsEx() == null)
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Menu items is a required field.");

                        /////////////////////////////////// End Validation /////////////////////////////////////////////

                        int revenueCenterID = Integer.parseInt(checkDetails.getpGuestCheck().revenue());

                        Operation operation = new Operation(Constants.RUNNING, "", new Date(), null, invokerUser.getId(),
                                account.getId(), operationType.getId(), revenueCenterID, false);

                        operationRepo.save(operation);

                        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                        SimphonyLocation location = generalSettings.getSimphonyLocationsByID(revenueCenterID);
                        ResponseEntity responseEntity= this.menuItemService.PostTransactionEx(checkDetails, location, operationType);

                        if(responseEntity.getStatusCode().isError()){
                            operation.setStatus(Constants.FAILED);
                            operation.setReason(responseEntity.getBody().toString());
                            operation.setEndDate(new Date());
                            operation.setRowsFetched(0);
                            createOrderService.saveOrderCreation(checkDetails, operation, account.getId());
                            operationRepo.save(operation);
                        }
                        else {
                            operation.setStatus(Constants.SUCCESS);
                            operation.setEndDate(new Date());
                            operation.setRowsFetched(1);
                            createOrderService.saveOrderCreation(checkDetails, operation, account.getId());
                            operationRepo.save(operation);
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
            e.printStackTrace();
            String message = e.getMessage();
            if(message == null || message.equals(""))
                message = "Please check the request data, then try again.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }
    }
}

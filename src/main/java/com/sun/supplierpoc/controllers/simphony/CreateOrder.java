package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@RestController()
@RequestMapping(value = {"/Simphony"})
public class CreateOrder {
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
    public ResponseEntity CreateOpenCheckRequest(@RequestParam(name = "revenueCenterID") int revenueCenterID,
                                                 @RequestHeader("Authorization") String authorization) {
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
                        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
                        SimphonyLocation location = generalSettings.getSimphonyLocationsByID(revenueCenterID);
                        return this.menuItemService.PostTransactionEx(location);
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

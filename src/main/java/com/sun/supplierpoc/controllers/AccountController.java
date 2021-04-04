package com.sun.supplierpoc.controllers;

import com.google.common.collect.Sets;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.OauthClientDetails;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.security.CustomClientDetailsService;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.RefreshTokenResult;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;


@RestController
public class AccountController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    CustomClientDetailsService customClientDetailsService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    MongoTemplate mongoTemplate;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Optional<Account> getAccount(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        return accountRepo.findByIdAndDeleted(user.getAccountId(), false);
    }

    @RequestMapping("/updateAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public boolean updateAccount(Principal principal, @RequestBody Account account){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        account = accountRepo.save(account);
        return account != null;
    }

    @RequestMapping("/updateAccountSyncTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public boolean updateAccountSyncTypes(Principal principal, @RequestBody Account account){
        addAccountSyncType(account);
        return true;
    }

    @RequestMapping(value = "/clientDetails")
    @ResponseBody
    public ResponseEntity<RefreshTokenResult> clientDetails(Authentication authentication) {
        OauthClientDetails oauthClientDetails = new OauthClientDetails();
        oauthClientDetails.setClientId("web-client");
        oauthClientDetails.setClientSecret("web-client-secret");
        oauthClientDetails.setSecretRequired(true);
        oauthClientDetails.setResourceIds(Sets.newHashSet("@ENTREPREWARE"));
        oauthClientDetails.setScope(Sets.newHashSet("all"));
        oauthClientDetails.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token","password"));
        oauthClientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localhost:8080"));
        oauthClientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        oauthClientDetails.setAccessTokenValiditySeconds(14400);
        oauthClientDetails.setRefreshTokenValiditySeconds(14400);
        oauthClientDetails.setAutoApprove(false);
        customClientDetailsService.addClientDetails(oauthClientDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user")
    @ResponseBody
    public ResponseEntity<RefreshTokenResult> addUser() {
        Set<GrantedAuthority> roles=new LinkedHashSet<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        User user = new User("user", "", null,"user","user",roles,true,
                true,true,true);
       /* user.setUsername("user");
        user.setPassword(new BCryptPasswordEncoder(12).encode("user"));
        user.setRoles();*/
        mongoTemplate.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping(value = "/addAccount")
    @ResponseBody
    public HashMap<String, Object> addAccount(@RequestBody Account account) {
        HashMap<String, Object> response = new HashMap<>();

        // check existence of account name
        if (accountRepo.existsAccountByNameAndDeleted(account.getName(), false)){
            response.put("message", "Account name already exits.");
            response.put("success", false);
            return response;
        }

        // create new account and user
        account = accountRepo.save(account);
        Set<GrantedAuthority> roles=new LinkedHashSet<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        User user = new User("admin", account.getId(), "","admin" + account.getName() ,"password",roles,true,
                true,true,true);
        userRepo.save(user);

        // Create General Settings
        GeneralSettings generalSettings = new GeneralSettings(account.getId(), new Date());
        generalSettingsRepo.save(generalSettings);

        // add default sync jobs to account
        boolean addingStatus = addAccountSyncType(account);

        if(addingStatus){
            response.put("message", "Account added successfully.");
            response.put("success", true);
        }else{
            response.put("message", "Failed to add new account.");
            response.put("success", false);
        }
        return response;
    }

    private boolean addAccountSyncType(Account account){
//            //suppliers
//            String syncDescription = "Used to sync suppliers from sun to my inventory daily.";
//            Configuration supplierConfig = new Configuration();
//            supplierConfig.supplierConfiguration = new SupplierConfiguration();
//
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                supplierConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType supplierSyncType = new SyncJobType(1,Constants.SUPPLIERS, syncDescription, "/suppliers",
//                    new Date(), supplierConfig, account.getId());
//            syncJobTypeRepo.save(supplierSyncType);
//
//            // Invoices
//            syncDescription = "Used to sync approved invoices from my inventory to sun daily.";
//            Configuration invoiceConfig = new Configuration();
//            invoiceConfig.invoiceConfiguration = new InvoiceConfiguration();
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                invoiceConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType invoiceSyncType = new SyncJobType(2, Constants.APPROVED_INVOICES, syncDescription, "/approvedInvoicesSun",
//                    new Date(), invoiceConfig, account.getId());
//            syncJobTypeRepo.save(invoiceSyncType);
//
//            // Credit Notes
//            syncDescription = "Used to sync credit notes from my inventory to sun daily.";
//
//            SyncJobType creditNotesSyncType = new SyncJobType(3, Constants.CREDIT_NOTES, syncDescription, "/creditNotesSun",
//                    new Date(), invoiceConfig, account.getId());
//            syncJobTypeRepo.save(creditNotesSyncType);
//
//            // Wastage
//            syncDescription = "Used to sync wastage from oracle hospitality reports to sun monthly.";
//            Configuration wastageConfig = new Configuration();
//            wastageConfig.wastageConfiguration = new WastageConfiguration();
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                wastageConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType wastageSyncType = new SyncJobType(4, Constants.WASTAGE, syncDescription, "/wastageSun",
//                    new Date(), wastageConfig, account.getId());
//            syncJobTypeRepo.save(wastageSyncType);
//
//            // Transfers
//            syncDescription = "Used to sync transfers from my inventory to sun monthly.";
//            Configuration transfersConfig = new Configuration();
//            transfersConfig.transferConfiguration = new TransferConfiguration();
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                transfersConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType transferSyncType = new SyncJobType(5, Constants.TRANSFERS, syncDescription, "/bookedTransferSun",
//                    new Date(), new Configuration(), account.getId());
//            syncJobTypeRepo.save(transferSyncType);
//
//            // Booked Production
//            syncDescription = "Used to sync booked production from my inventory to sun monthly.";
//            SyncJobType bookedProductionSyncType = new SyncJobType(6, Constants.BOOKED_PRODUCTION, syncDescription,
//                    "/bookedProductionSun", new Date(), new Configuration(), account.getId());
//            syncJobTypeRepo.save(bookedProductionSyncType);
//
//            // Sales
//             syncDescription = "Used to sync sales from oracle hospitality reports to sun monthly.";
//            Configuration salesConfig = new Configuration();
//            salesConfig.salesConfiguration = new SalesConfiguration();
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                salesConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType salesSyncType = new SyncJobType(7, Constants.SALES, syncDescription, "/posSalesSun",
//                    new Date(), salesConfig, account.getId());
//            syncJobTypeRepo.save(salesSyncType);
//
//            // Consumption
//            syncDescription = "Used to sync consumption from oracle hospitality reports to sun monthly.";
//            Configuration consumptionConfig = new Configuration();
//            consumptionConfig.consumptionConfiguration = new ConsumptionConfiguration();
//            if(account.getERD().equals(Constants.SUN_ERD) || account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
//                consumptionConfig.inforConfiguration = new InforConfiguration();
//            }
//
//            SyncJobType consumptionSyncType = new SyncJobType(8, Constants.CONSUMPTION, syncDescription, "/consumptionSun",
//                    new Date(), consumptionConfig, account.getId());
//            syncJobTypeRepo.save(consumptionSyncType);
//
//            // Menu Items
//            syncDescription = "Used to sync simphony menu items.";
//            Configuration menuItemConfig = new Configuration();
//            menuItemConfig.menuItemConfiguration = new MenuItemConfiguration();
//
//            SyncJobType menuItemsSyncType = new SyncJobType(9, Constants.MENU_ITEMS, syncDescription, "/menuItems",
//                    new Date(), menuItemConfig, account.getId());
//            syncJobTypeRepo.save(menuItemsSyncType);

        // OPERA Reservation
/*        String syncDescription = "Used to sync reservation from opera.";
        Configuration wlsIntegration = new Configuration();
        wlsIntegration.supplierConfiguration = new SupplierConfiguration();

        SyncJobType supplierSyncType = new SyncJobType(1,Constants.wLsIntegration, syncDescription, "/2wLsIntegration",
                new Date(), wlsIntegration, account.getId());
        syncJobTypeRepo.save(supplierSyncType);

        // OPERA New Booking
        syncDescription = "Used to sync new booking from opera.";
        Configuration newBookingConfig = new Configuration();

        SyncJobType newBookingSyncType = new SyncJobType(10, Constants.NEW_BOOKING_REPORT, syncDescription,
                "/newBookingReport", new Date(), newBookingConfig, account.getId());
        syncJobTypeRepo.save(newBookingSyncType);*/

        // OPERA Cancel Booking
        String syncDescription = "Used to sync cancel booking from opera.";
        Configuration cancelBookingConfig = new Configuration();

        SyncJobType cancelBookingSyncType = new SyncJobType(11, Constants.CANCEL_BOOKING_REPORT, syncDescription,
                "/cancelBookingReport", new Date(), cancelBookingConfig, account.getId());
        syncJobTypeRepo.save(cancelBookingSyncType);

        // OPERA Occupancy Update
        syncDescription = "Used to sync occupancy update from opera.";
        Configuration occupancyUpdateConfig = new Configuration();

        SyncJobType occupancyUpdateSyncType = new SyncJobType(11, Constants.OCCUPANCY_UPDATE_REPORT, syncDescription,
                "/occupancyUpdateReport", new Date(), occupancyUpdateConfig, account.getId());
        syncJobTypeRepo.save(occupancyUpdateSyncType);

        return true;

    }

    public ArrayList<Account> getAccounts() {
        return (ArrayList<Account>) accountRepo.findAll();
    }

}

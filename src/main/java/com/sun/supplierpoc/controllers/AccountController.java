package com.sun.supplierpoc.controllers;

import com.google.common.collect.Sets;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Configuration;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.OauthClientDetails;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.security.CustomClientDetailsService;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.RefreshTokenResult;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

@EnableResourceServer

@RestController
public class AccountController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
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
        return accountRepo.findById(user.getAccountId());
    }

    @RequestMapping("/updateAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public boolean updateAccount(Principal principal, @RequestBody Account account){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        account = accountRepo.save(account);
        return account != null;
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
        oauthClientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localConstants.HOST:8080"));
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
        if (accountRepo.existsAccountByName(account.getName())){
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

        // add default sync jobs to account
        addAccountSyncType(account);

        response.put("message", "Account added successfully.");
        response.put("success", true);
        return response;
    }

    public boolean addAccountSyncType(Account account){
        // suppliers
        String syncDescription = "Used to sync suppliers from sun to my inventory daily.";
        SyncJobType supplierSyncType = new SyncJobType(Constants.SUPPLIERS, syncDescription, "/suppliers",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(supplierSyncType);

        // Invoices
        syncDescription = "Used to sync approved invoices from my inventory to sun daily.";
        SyncJobType invoiceSyncType = new SyncJobType(Constants.APPROVED_INVOICES, syncDescription, "/approvedInvoices",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(invoiceSyncType);

        // Credit Notes
        syncDescription = "Used to sync credit notes from my inventory to sun daily.";
        SyncJobType creditNotesSyncType = new SyncJobType(Constants.CREDIT_NOTES, syncDescription, "/creditNotes",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(creditNotesSyncType);

        // Transfers
        syncDescription = "Used to sync transfers from my inventory to sun monthly.";
        SyncJobType transferSyncType = new SyncJobType(Constants.TRANSFERS, syncDescription, "/bookedTransferSun",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(transferSyncType);

        // Consumption
        syncDescription = "Used to sync consumption from oracle hospitality reports to sun monthly.";
        SyncJobType consumptionSyncType = new SyncJobType(Constants.CONSUMPTION, syncDescription, "/journalsSun",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(consumptionSyncType);

        // Wastage
        syncDescription = "Used to sync wastage from oracle hospitality reports to sun monthly.";
        SyncJobType wastageSyncType = new SyncJobType(Constants.WASTAGE, syncDescription, "/wastageSun",
                new Date(), new Configuration(), account.getId());
        syncJobTypeRepo.save(wastageSyncType);

        return true;
    }



    public ArrayList<Account> getAccounts() {
        return (ArrayList<Account>) accountRepo.findAll();
    }

}

package com.sun.supplierpoc.controllers;

import com.google.common.collect.Sets;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.auth.OauthClientDetails;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.security.CustomClientDetailsService;
import com.sun.supplierpoc.models.auth.User;
import org.apache.commons.validator.routines.EmailValidator;
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
    public Account getAccount(Principal principal){
        try{
            User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> account = accountRepo.findByIdAndDeleted(user.getAccountId(), false);
            return account.get();
        } catch (Exception e) {
            e.printStackTrace();
            return new Account();
        }
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
        oauthClientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localhost:8080"));
        oauthClientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        oauthClientDetails.setAccessTokenValiditySeconds(14400);
        oauthClientDetails.setRefreshTokenValiditySeconds(14400);
        oauthClientDetails.setAutoApprove(false);
        customClientDetailsService.addClientDetails(oauthClientDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/addAdminUser")
    @ResponseBody
    public ResponseEntity addAdminUser(Principal principal,
                                                           @RequestParam("addFlag") boolean addFlag,
                                                           @RequestBody User userRequest) {

        HashMap response = new HashMap();

        User authedUser = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            User user = new User();

            if(addFlag) {

                if (userRepo.existsByUsernameAndAccountId(userRequest.getUsername(), account.getId())) {
                    response.put("message", "User is already exist with this username.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                Set<GrantedAuthority> roles = new LinkedHashSet<>();
                roles.add(new SimpleGrantedAuthority("ROLE_USER"));
                user.setName(userRequest.getName().replaceAll("\\s", ""));
                user.setUsername(userRequest.getUsername().replaceAll("\\s", ""));
                user.setPassword(userRequest.getPassword());
                user.setAccountId(account.getId());
                user.setAuthorities(roles);
                user.setEnabled(true);
                user.setAccountNonExpired(true);
                user.setAccountNonLocked(true);
                user.setCredentialsNonExpired(true);
                user.setCreationDate(new Date());
                user.setUpdateDate(new Date());
                user.setEmail(userRequest.getEmail());

            }else {
                Optional<User> updatedUserOptional = userRepo.findById(userRequest.getId());
                if(updatedUserOptional.isPresent()){

                    user = updatedUserOptional.get();

                    if(!user.getUsername().equals(userRequest.getUsername())){
                        if (userRepo.existsByUsernameAndAccountId(userRequest.getUsername(), account.getId())) {
                            response.put("message", "This username is already used.");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                    }

                    user.setName(userRequest.getName().replaceAll("\\s", ""));
                    user.setUsername(userRequest.getUsername().replaceAll("\\s", ""));
                    user.setPassword(userRequest.getPassword());
                    user.setUpdateDate(new Date());

                }else{
                    response.put("message", "Can't find the user.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            mongoTemplate.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            response.put("message", Constants.INVALID_USER);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping("/deleteUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteUsers(@RequestParam(name = "addFlag") boolean addFlag,
                                      @RequestParam("userId") String userId, Principal principal) {

        HashMap response = new HashMap();

        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

        if (accountOptional.isPresent()) {

            Optional<User> updatedUserOptional = userRepo.findById(userId);
            if(updatedUserOptional.isPresent()){

                User user = updatedUserOptional.get();

                user.setDeleted(addFlag);
                user.setAccountNonExpired(!addFlag);
                user.setAccountNonLocked(!addFlag);
                user.setCredentialsNonExpired(!addFlag);
                user.setEnabled(!addFlag);
                mongoTemplate.save(user);

            }
            if(addFlag) {
                response.put("message", "User deleted Successfully.");
            }else{
                response.put("message", "User restored Successfully.");
            }
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
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
        User user = new User("admin", account.getId(), "","admin" + account.getDomain() ,"password",roles,true,
                true,true,true);
        userRepo.save(user);

        // Create General Settings
        GeneralSettings generalSettings = new GeneralSettings(account.getId(), new Date());
        generalSettingsRepo.save(generalSettings);

        // add default sync jobs to account
        boolean addingStatus = true;

        // add default operation types to account

        if(addingStatus){
            response.put("message", "Account added successfully.");
            response.put("success", true);
        }else{
            response.put("message", "Failed to add new account.");
            response.put("success", false);
        }
        return response;
    }


    public ArrayList<Account> getAccounts() {
        return (ArrayList<Account>) accountRepo.findAll();
    }



}

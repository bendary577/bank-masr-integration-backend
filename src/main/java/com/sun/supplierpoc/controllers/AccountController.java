package com.sun.supplierpoc.controllers;

import com.google.common.collect.Sets;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.OauthClientDetails;
import com.sun.supplierpoc.services.security.CustomClientDetailsService;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.RefreshTokenResult;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.bson.types.ObjectId;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@EnableResourceServer

@RestController
public class AccountController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${oauth.auth.url}")
    private String authUrl;

    @Value("${oauth.token.url}")
    private String accessTokenUrl;

    @Value("${oauth.token.refresh.url}")
    private String refreshTokenUrl;

    @Value("${oauth.id.secret}")
    private String encodedIdSecret;
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

    public ArrayList<Account> getAccounts() {
        return (ArrayList<Account>) accountRepo.findAll();
    }

}

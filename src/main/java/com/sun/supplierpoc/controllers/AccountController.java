package com.sun.supplierpoc.controllers;

import com.google.common.collect.Sets;
import com.sun.supplierpoc.conf.CustomClientDetailsService;
import com.sun.supplierpoc.models.AccessTokenResponse;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.RefreshTokenResult;
import com.sun.supplierpoc.models.auth.MongoClientDetails;
import com.sun.supplierpoc.models.auth.MongoUser;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    CustomClientDetailsService customClientDetailsService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping("/getAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Optional<Account> getAccount(){
        // get accountID from user 5e4bd1a7b334d338f81d9b9a
        return accountRepo.findById("5e4bd1a7b334d338f81d9b9a");
    }

    @RequestMapping(value = "/clientDetails")
    @ResponseBody
    public ResponseEntity<RefreshTokenResult> clientDetails(Authentication authentication) {
        MongoClientDetails clientDetails = new MongoClientDetails();
        clientDetails.setClientId("web-client");
        clientDetails.setClientSecret("web-client-secret");
        clientDetails.setSecretRequired(true);
        clientDetails.setResourceIds(Sets.newHashSet("@ENTREPREWARE"));
        clientDetails.setScope(Sets.newHashSet("all"));
        clientDetails.setAuthorizedGrantTypes(Sets.newHashSet("authorization_code", "refresh_token","password"));
        clientDetails.setRegisteredRedirectUri(Sets.newHashSet("http://localhost:8080"));
        clientDetails.setAuthorities(AuthorityUtils.createAuthorityList("ROLE_USER"));
        clientDetails.setAccessTokenValiditySeconds(14400);
        clientDetails.setRefreshTokenValiditySeconds(14400);
        clientDetails.setAutoApprove(false);
        customClientDetailsService.addClientDetails(clientDetails);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user")
    @ResponseBody
    public ResponseEntity<RefreshTokenResult> adduser(Principal principal) {
        Set<GrantedAuthority> roles=new LinkedHashSet<>();
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        MongoUser mongoUser = new MongoUser(null,"user","user",roles,true,true,true,true);
       /* mongoUser.setUsername("user");
        mongoUser.setPassword(new BCryptPasswordEncoder(12).encode("user"));
        mongoUser.setRoles();*/
        mongoTemplate.save(mongoUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

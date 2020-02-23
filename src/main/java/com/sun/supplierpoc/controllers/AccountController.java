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
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
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

    @RequestMapping("/getAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Optional<Account> getAccount(){
        // get accountID from user 5e4bd1a7b334d338f81d9b9a
        return accountRepo.findById("5e4bd1a7b334d338f81d9b9a");
    }
}

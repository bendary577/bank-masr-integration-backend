package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/server")

public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    private AccountRepo accountRepo;


    @RequestMapping("/getUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<User> getAccount(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        return userRepo.findByAccountId(account.getId());
    }
}

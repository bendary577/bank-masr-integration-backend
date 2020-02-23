package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.repositories.AccountRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController

public class AccountController {
    @Autowired
    private AccountRepo accountRepo;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getAccount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Optional<Account> getAccount(){
        // get accountID from user 5e4bd1a7b334d338f81d9b9a
        return accountRepo.findById("5e4bd1a7b334d338f81d9b9a");
    }
}

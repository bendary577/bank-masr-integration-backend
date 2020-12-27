package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    InvokerUserRepo invokerUserRepo;
    @Autowired
    private AccountRepo accountRepo;


    @RequestMapping("/getUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<User> getUsers(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        return userRepo.findByAccountIdAndDeleted(account.getId(), false);
    }

    @RequestMapping("/addInvokerUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<String> addInvokerUser(@RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                         @RequestBody InvokerUser invoker, Principal principal){
        try {
            User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()){
                Account account = accountOptional.get();

                invoker.setAccountId(account.getId());
                invoker.setSyncJobTypeId(syncJobTypeId);

                InvokerUser invokerUser = new InvokerUser(invoker.getUsername(), invoker.getPassword(), account.getId(),
                        syncJobTypeId, new Date());
                invokerUserRepo.save(invokerUser);

                return ResponseEntity.status(HttpStatus.OK).body("Add web service invoker successfully.");
            }else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
        }
    }
}

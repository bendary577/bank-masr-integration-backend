package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Application;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.ApplicationRepo;
import com.sun.supplierpoc.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class ApplicationController {

    @Autowired
    ApplicationRepo applicationRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountService accountService;

    private Conversions conversion = new Conversions();

    @GetMapping("/getApplications")
    public List<Application> getApplications(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            List<Application> applications = applicationRepo.findAllByAccountIdAndDeleted(account.getId(), false);
            return applications;
        } else {
            return new ArrayList<>();
        }
    }

    @PostMapping("/createApplication")
    public ResponseEntity<?> createApplication(@RequestBody Application application,
                                                   BindingResult result, Principal principal) {

        HashMap response = new HashMap();

        try {
            if (result.hasErrors()) {
                response.put("error", result.getAllErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Account account = accountService.getAccount(user.getAccountId());

            String endPoint = conversion.toCamelCase(application.getName());
            application.setEndPoint(endPoint);
            application.setCreationDate(new Date());
            application.setDeleted(false);
            application.setAccountId(account.getId());
            applicationRepo.save(application);

            response.put("isSuccess", Constants.SUCCESS);
            response.put("transactionType", application);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {

            response.put("isSuccess", Constants.FAILED);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }
    }
}

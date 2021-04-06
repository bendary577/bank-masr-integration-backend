package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.application.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    @RequestMapping("/getTransactions")
    public List<Transactions> getTransactionByType(Principal principal,
                                                   @RequestParam("transactionType") String transactionType) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            List<Transactions> transactions = transactionService.getTransactionByType(transactionType, account);

            return transactions;
        }else{
            return new ArrayList<>();
        }
    }

    @RequestMapping("/getTotalSpendTransactions")
    public ResponseEntity getTotalSpendTransactions(Principal principal,@RequestParam("transactionType") String transactionType,
                                                   @RequestParam("dateFlag") String dateFlag) {

        HashMap response = new HashMap();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            double totalSpend = transactionService.getTotalSpendTransactions(dateFlag, transactionType, account);

            response.put("totalSpend", totalSpend);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }
    }

    @PostMapping("/createTransactionType")
    public ResponseEntity<?> createTransactionType(Principal principal,
                                                   @RequestBody TransactionType transactionType, BindingResult result) {

        HashMap response = new HashMap();

        try {

            if (result.hasErrors()) {
                response.put("error", result.getAllErrors());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Account account = accountService.getAccount(user.getAccountId());

            transactionService.createTransactionType(account, transactionType);

            response.put("isSuccess", Constants.SUCCESS);
            response.put("transactionType", transactionType);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {

            response.put("isSuccess", Constants.FAILED);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        }
    }


}

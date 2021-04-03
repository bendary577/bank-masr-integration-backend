package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.TransactionTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private TransactionTypeRepo transactionTypeRepo;

    private Conversions conversions = new Conversions();

    public Optional<Account> getAccountOptional(String accountID) {
        return accountRepo.findById(accountID);
    }

    public Account getAccount(String accountID) {

        Optional<Account> accountOptional = accountRepo.findById(accountID);

        if(accountOptional.isPresent()) {
            return accountOptional.get();
        }else {
            return new Account();
        }
    }
    public Account getAuthenticatedAccount(InvokerUser user) {
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
        return account;
    }
}

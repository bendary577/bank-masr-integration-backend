package com.sun.supplierpoc.controllers.PurchasOder;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
public class PoController {

    Logger log = LoggerFactory.getLogger(PoController.class);

    @Autowired
    private PorderService porderService;

    @Autowired
    AccountRepo accountRepo;

    @Autowired
    UserRepo userRepo;

    @PostMapping("/api/purchase")
    public ResponseEntity<?> createPurchaseOrder(@RequestBody PorderRequest porderRequest, BindingResult result,
                                                 Principal principal) throws InterruptedException {

        HashMap<String, Object> response = new HashMap<>();

        User user = userRepo.findByUsername("admin");

        Account account = account(user);

        String po = porderService.CreatePurchase(
                account, porderRequest);

        response.put("purchase Order", po);

        return new ResponseEntity<>(po, HttpStatus.OK);
    }

    public Account account(User user){
        Account account1 = new Account();
        AccountCredential accountCredential = new AccountCredential();

        accountCredential.setAccount("HospitalityOHIM");
        accountCredential.setUsername("IFC");
        accountCredential.setCompany("ACT");
        accountCredential.setPassword("Mic#2000");

                ArrayList < AccountCredential > accountCredentials = new ArrayList<>();
        accountCredentials.add(accountCredential);

        account1.setAccountCredentials(accountCredentials);

        return account1;
    }
}

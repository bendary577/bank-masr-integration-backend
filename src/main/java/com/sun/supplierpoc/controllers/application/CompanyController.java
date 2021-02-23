package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.Company;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.CompanyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@RestController

public class CompanyController {
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    CompanyRepo companyRepo;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApplicationCompanies")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationCompanies(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<Company> companies = companyRepo.findAllByAccountID(account.getId());
            return  ResponseEntity.status(HttpStatus.OK).body(companies);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationCompany")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationCompany(@RequestParam(name = "addFlag") boolean addFlag,
            @RequestBody Company company, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            if(addFlag){
                company.setAccountID(account.getId());
                company.setCreationDate(new Date());
                company.setLastUpdate(new Date());
                company.setDeleted(false);
            }else {
                company.setLastUpdate(new Date());
            }

            companyRepo.save(company);

            return ResponseEntity.status(HttpStatus.OK).body(company);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/deleteApplicationCompanies")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationCompanies(@RequestBody ArrayList<Company> companies, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Company company : companies) {
                company.setDeleted(true);
                companyRepo.save(company);
            }
            return ResponseEntity.status(HttpStatus.OK).body(companies);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
}

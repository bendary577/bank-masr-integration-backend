package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController

public class GroupController {

    @Autowired
    AccountRepo accountRepo;
    @Autowired
    GroupRepo groupRepo;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationCompanies(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<Group> groups = groupRepo.findAllByAccountID(account.getId());
            return  ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationCompany(@RequestParam(name = "addFlag") boolean addFlag,
            @RequestBody Group group, Principal principal){

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            if(addFlag){
                group.setAccountID(account.getId());
                group.setCreationDate(new Date());
                group.setLastUpdate(new Date());
                group.setDeleted(false);
            }else {
                group.setLastUpdate(new Date());
            }

            groupRepo.save(group);
            return ResponseEntity.status(HttpStatus.OK).body(group);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/deleteApplicationGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationCompanies(@RequestBody List<Group> groups, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (Group group : groups) {
                group.setDeleted(true);
                groupRepo.save(group);
            }
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }
}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.ActionService;
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
public class UserController {
    @Autowired
    UserRepo userRepo;

    @Autowired
    InvokerUserRepo invokerUserRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private ActionService actionService;


    @RequestMapping("/getUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<User> getUsers(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        return userRepo.findByAccountId(account.getId());
    }

    @RequestMapping("/updateUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity updateUser(Principal principal, @RequestBody User updatedUser) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                updatedUser.setUpdateDate(new Date());
                userRepo.save(updatedUser);
                return ResponseEntity.status(HttpStatus.OK).body("");
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
        }
    }

    @RequestMapping("/addInvokerUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addInvokerUser(@RequestBody InvokerUser invoker, Principal principal) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                invoker.setAccountId(account.getId());
                invoker.setTypeId(invoker.getTypeId());

                // check existence
                if (invokerUserRepo.countAllByUsername(invoker.getUsername()) > 0) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists!");
                } else {
                    InvokerUser invokerUser = new InvokerUser(invoker.getUsername(), invoker.getPassword(), account.getId(),
                            invoker.getTypeId(), new Date());
                    invokerUserRepo.save(invokerUser);
                    return ResponseEntity.status(HttpStatus.OK).body("");
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add web service invoker.");
        }
    }

    @RequestMapping("/getInvokerUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getInvokerUser(@RequestParam(name = "syncJobTypeId") String syncJobTypeId) {
        try {
            ArrayList<InvokerUser> invokerUsers = invokerUserRepo.findAllByTypeId(syncJobTypeId);
            return ResponseEntity.status(HttpStatus.OK).body(invokerUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get web service invoker.");
        }
    }

    /* Entry System Web Services */

    @RequestMapping("/getAgentActions")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getAgentActions(Principal principal,
                                          @RequestParam(name = "userId") String userId,
                                          @RequestParam(name = "actionType") String actionType) {
        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            if(user == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_USER);
            }

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                ArrayList<Action> actions = actionService.getUserAction(userId, account.getId(), actionType);
                return ResponseEntity.status(HttpStatus.OK).body(actions);
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.INVALID_ACCOUNT);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}

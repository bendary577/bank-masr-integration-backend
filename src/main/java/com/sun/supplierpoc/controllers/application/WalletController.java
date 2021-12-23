package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.applications.ActionType;
import com.sun.supplierpoc.models.applications.Balance;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.services.ActionService;
import com.sun.supplierpoc.services.RoleService;
import com.sun.supplierpoc.services.application.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private RoleService roleService;

    @Autowired
    private WalletService walletService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/chargeWallet")
    public ResponseEntity<?> chargeWallet(@RequestParam("userId") String userId,
                                          @RequestBody Balance balance,
                                          Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                if (roleService.hasRole(authedUser, Constants.CHARGE_WALLET)) {
                    response = walletService.chargeWallet(authedUser, userId, balance);

                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/deductFromWallet")
    public ResponseEntity<?> deductFromWallet(@RequestParam("userId") String userId,
                                              @RequestParam("amount") double amount,
                                              Principal principal){

        Response response = new Response();
        User authedUser = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(authedUser != null){

            Optional<Account> accountOptional = accountRepo.findById(authedUser.getAccountId());

            if(accountOptional.isPresent()) {
                if (roleService.hasRole(authedUser, Constants.DEDUCT_WALLET)) {

                    response = walletService.deductWallet(userId, amount);

                    if(response.isStatus()){
                    return new ResponseEntity<>(response, HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.NOT_ELIGIBLE_USER);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
            }
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}

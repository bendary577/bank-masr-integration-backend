package com.sun.supplierpoc.controllers.application.talabat;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;

import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.TalabatIntegratorService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/talabat")
public class TalabatIntegratorController {

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private TalabatIntegratorService talabatIntegratorService;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<?> SyncTalabatOrders(Principal principal){

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if(accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = talabatIntegratorService.sendReceivedOrders(account);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }else{
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("branch")
    public ResponseEntity<?> getBranchOrders(@RequestParam("branch") String branch){

//        Response response = talabatRestService.loginRequest(new A);

//        response = talabatRestService.getOrders((Token) response.getData(), branch);

        return new ResponseEntity<>("response", HttpStatus.OK);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestBody Token token){

//        Response response = talabatRestService.getOrders(token);

//        talabatIntegratorService.sendReceivedOrders( (TalabatOrder) response.getData() );

        return new ResponseEntity<>("response", HttpStatus.OK);
    }

    @PostMapping("/order")
    public ResponseEntity<?> getOrderById(@RequestBody RestOrder order){

//        Response response = talabatRestService.loginRequest(new Account());

//        response = talabatRestService.getOrderById(order, (Token) response.getData());

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @GetMapping("/fetchFoodicsProduct")
    public ResponseEntity<?> fetchProducts(Principal principal){

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(user != null){

            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

            if(accountOptional.isPresent()){

                Account account = accountOptional.get();

                response = talabatRestService.fetchProducts(account);

                if(response.isStatus()){
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else{
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            }else{
                response.setMessage(Constants.INVALID_ACCOUNT);
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }else {
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

}

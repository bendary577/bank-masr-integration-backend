package com.sun.supplierpoc.controllers.application.talabat;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;

import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.foodics.Product;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.TalabatIntegratorService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Optional;

@RestController
public class TalabatIntegratorController {

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private TalabatIntegratorService talabatIntegratorService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InvokerUserService invokerUserService;

    @GetMapping("/talabat")
    public ResponseEntity<?> SyncTalabatOrders(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = talabatIntegratorService.syncFoodicsOrders(account);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/talabat/senToFoodics")
    public ResponseEntity<?> sendTalabatOrders(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = talabatIntegratorService.sendReceivedOrders(account);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/talabat/branch")
    public ResponseEntity<?> getBranchOrders(Principal principal,
                                             @RequestParam("branch") String branch) {
        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = talabatIntegratorService.syncFoodicsBranchOrders(account, branch);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/talabat/order")
    public ResponseEntity<?> getOrderById(Principal principal,
                                          @RequestBody RestOrder order) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = talabatIntegratorService.getOrderDtails(account, order);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/talabat/orders")
    public ResponseEntity<?> getOrderById(@RequestBody RestOrder order) {

//        Response response = talabatRestService.loginRequest(new Account());

//        response = talabatRestService.getOrderById(order, (Token) response.getData());

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @GetMapping("/foodicsFetchProducts")
    public ResponseEntity<?> fetchProducts(Principal principal) {

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if (user != null) {

            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

            if (accountOptional.isPresent()) {

                Account account = accountOptional.get();

                response = talabatIntegratorService.fetchProducts(account);

                if (response.isStatus()) {
                    return new ResponseEntity<>(response, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

            } else {
                response.setMessage(Constants.INVALID_ACCOUNT);
                response.setStatus(false);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        } else {
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping("/foodics/webhook/products")
    public ResponseEntity<?> updateFoodicsRequest(@RequestHeader("Authorization") String authorization,
                                                  @RequestBody Product foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = talabatIntegratorService.updateFoodicsProduct(account, foodicsProduct);

            if (response.get("status").equals("success")) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } else {
            response.put("message", Constants.INVALID_USER);
            response.put("status", "failed");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping("/foodics/webhook/orders")
    public ResponseEntity<?> updateFetchOrder(@RequestHeader("Authorization") String authorization,
                                              @RequestBody FoodicsOrder foodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = talabatIntegratorService.updateFoodicsOrder(account, foodicsOrder);

            if (response.get("status").equals("success")) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

        } else {
            response.put("message", Constants.INVALID_USER);
            response.put("status", "failed");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

}

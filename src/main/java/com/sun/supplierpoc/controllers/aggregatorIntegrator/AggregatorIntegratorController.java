package com.sun.supplierpoc.controllers.aggregatorIntegrator;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.foodics.Product;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.onlineOrdering.AggregatorIntegratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Optional;

@RestController
@RequestMapping("/aggregator")
public class AggregatorIntegratorController {

    @Autowired
    private AggregatorIntegratorService aggregatorIntegratorService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InvokerUserService invokerUserService;

    @GetMapping("/products")
    public ResponseEntity<?> fetchProducts(Principal principal) {

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if (user != null) {

            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

            if (accountOptional.isPresent()) {

                Account account = accountOptional.get();

                response = aggregatorIntegratorService.fetchProducts(account);

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

    @RequestMapping("/webhook/products")
    public ResponseEntity<?> updateFoodicsRequest(@RequestHeader("Authorization") String authorization,
                                                  @RequestBody Product foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = aggregatorIntegratorService.updateFoodicsProduct(account, foodicsProduct);

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

    @RequestMapping("/webhook/orders")
    public ResponseEntity<?> updateFetchOrder(@RequestHeader("Authorization") String authorization,
                                              @RequestBody FoodicsOrder foodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = aggregatorIntegratorService.updateFoodicsOrder(account, foodicsOrder);

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

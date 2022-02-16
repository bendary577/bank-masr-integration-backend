package com.sun.supplierpoc.controllers.application.talabat;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;

import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.TalabatConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.FoodicsProduct;
import com.sun.supplierpoc.models.Order;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.InvokerUserRepo;
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
import java.util.HashMap;
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

    @GetMapping("/foodics/fetchFoodicsProduct")
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
                                                  @RequestParam("productId") String productId,
                                                  @RequestBody HashMap foodicsProduct) {

        HashMap<String, Object> response = new HashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

//            FoodicsProduct foodicsProduct1 = talabatIntegratorService.updateFoodicsProdu(account, foodicsProduct);

            response.put("data", foodicsProduct);
            response.put("message", "Product Saved Successfully.");
            response.put("status", "Success");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            response.put("message", Constants.INVALID_USER);
            response.put("status", "Faild");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping("/foodics/webhook/orders")
    public ResponseEntity<?> updateFetchorder(@RequestHeader("Authorization") String authorization,
                                           @RequestParam("orderId") String orderId,
                                           @RequestBody HashMap foodicsOrder) {

        HashMap<String, Object> response = new HashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

//            FoodicsOrder foodicsOrder1 = talabatIntegratorService.updateFoodicsOrder(account, foodicsOrder);

            response.put("data", foodicsOrder);
            response.put("message", "Order Saved Successfully.");
            response.put("status", "Success");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } else {
            response.put("message", Constants.INVALID_USER);
            response.put("status", "Faild");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
    }

}

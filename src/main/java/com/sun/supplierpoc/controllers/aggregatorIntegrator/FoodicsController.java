package com.sun.supplierpoc.controllers.aggregatorIntegrator;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.TalabatIntegratorService;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;

@RestController
@RequestMapping("/foodics")
public class FoodicsController {
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TalabatIntegratorService talabatIntegratorService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private FoodicsWebServices foodicsWebServices;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @RequestMapping("/webhook/products")
    public ResponseEntity<?> updateFoodicsProduct(@RequestHeader("Authorization") String authorization,
                                                  @RequestBody FoodicsProduct foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = talabatIntegratorService.updateTalabatProduct(account, foodicsProduct);

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
    public ResponseEntity<?> updateFoodicsOrder(@RequestHeader("Authorization") String authorization,
                                                @RequestBody FoodicsOrder foodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());

        if (account != null) {

            response = talabatIntegratorService.updateTalabatOrder(account, foodicsOrder);

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

    @RequestMapping("/products")
    public ResponseEntity<?> foodicsProducts(@RequestHeader("Authorization") String authorization) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
        Account account = accountService.getAccount(invokerUser.getAccountId());
        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

        if (account != null) {

            response = (LinkedHashMap<String, Object>) foodicsWebServices.fetchFoodicsProducts(generalSettings.getAggregatorConfiguration().getFoodicsAccountData());

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

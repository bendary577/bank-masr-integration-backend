package com.sun.supplierpoc.controllers.aggregatorIntegrator;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AggregatorOrder;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SendEmailService;
import com.sun.supplierpoc.services.TalabatIntegratorService;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private SendEmailService sendEmailService;

    @Autowired
    private UserRepo userRepo;

    @RequestMapping("/webhook/products")
    public ResponseEntity<?> updateFoodicsProduct(@RequestHeader("Authorization") String authorization,
                                                  @RequestBody FoodicsProduct foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

        Account account = accountService.getAccount(invokerUser.getAccountId());

        List<User> users = userRepo.findByAccountIdAndDeleted(account.getId(), false);
        User user;

        if(users.isEmpty()){
            response.put("message", Constants.INVALID_USER);
            response.put("status", "failed");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }else{
            user = users.get(0);
        }

        if (account != null) {

            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            response = talabatIntegratorService.updateTalabatProduct(foodicsProduct, generalSettings);

            if (response.get("status").equals("success")) {
                FoodicsProduct updatedFoodicsProduct = (FoodicsProduct) response.get("data");
                generalSettingsRepo.save(generalSettings);
                sendEmailService.sendFoodicsProductUpdatedMail(user, updatedFoodicsProduct );
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


}

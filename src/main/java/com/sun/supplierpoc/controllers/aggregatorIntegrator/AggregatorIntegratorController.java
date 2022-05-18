package com.sun.supplierpoc.controllers.aggregatorIntegrator;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AggregatorOrder;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.onlineOrdering.AggregatorIntegratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

@RestController
@RequestMapping("/aggregator")
public class AggregatorIntegratorController {
    @Autowired
    private OrderRepo orderRepo;

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

    @GetMapping("/orders")
    public ResponseEntity<?> sendTalabatOrders(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response = aggregatorIntegratorService.sendTalabatOrdersToFoodics(account);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/storedOrders")
    public ResponseEntity<?> getstoredOrders(Principal principal) {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            response.setStatus(true);
            response.setMessage("");

//            LocalDate startDate = LocalDate.of( 2022 , Month.MAY , 16 );
//            LocalDate endDate = LocalDate.of( 2022 , Month.MAY , 17 );
//
//            String sDate6 = "31-Dec-1998 23:37:50";
//            SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");

            ArrayList<AggregatorOrder> aggregatorOrders = (ArrayList<AggregatorOrder>) orderRepo.findTop15ByAccountOrderByCreationDateDesc(account);
            response.setData(aggregatorOrders);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_ACCOUNT);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

}

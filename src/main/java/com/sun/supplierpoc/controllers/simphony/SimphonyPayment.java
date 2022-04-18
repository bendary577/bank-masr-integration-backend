package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.PosMachineMap;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.SimphonyPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;

@RestController
public class SimphonyPayment{
    @Autowired
    private SimphonyPaymentService simphonyPaymentService;
    @Autowired
    private InvokerUserService invokerUserService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepo accountRepo;

    private SimpleClientHttpRequestFactory getClientHttpRequestFactory()
    {
        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(100_000);
        //Read timeout
        clientHttpRequestFactory.setReadTimeout(100_000);
        return clientHttpRequestFactory;
    }

    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

    @PostMapping("/test/simphonyPayment/paySplitCheck")
    public ResponseEntity<?> paymentTransaction(@RequestHeader("Authorization") String authorization,
                                                @Valid @RequestBody SimphonyPaymentReq simphonyPayment,
                                                BindingResult result) {
        Response response = new Response();

        ResponseEntity paxResult = restTemplate.getForEntity("http://192.168.1.35:5050" + "?transactionAmount=" +
                Math.round(1f) + "&currency=" + "eg" + "&transType=" + "12", String.class);

        paxResult.getBody();

//        try{
//            if(!result.hasErrors()) {
//
//                InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
//
//                if(invokerUser != null) {
//
//
//                    Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());
//
//                    if (accountOptional.isPresent()) {
//
//                        Account account = accountOptional.get();
//                        response = simphonyPaymentService.createSimphonyPaymentTransaction(simphonyPayment, account);
//
//                    } else {
//                        response.setStatus(false);
//                        response.setMessage(Constants.ACCOUNT_NOT_EXIST);
//                    }
//                }else{
//
//                    response.setStatus(false);
//                    response.setMessage(Constants.INVALID_USER);
//
//                }
//
//            }else{
//
//            response.setStatus(false);
//            response.setMessage(result.getAllErrors().get(0).getDefaultMessage());
//
//            }
//        }catch(Exception e){
//            response.setStatus(false);
//            response.setMessage(e.getMessage());
//        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/simphonyPayment/simphonyCheck")
    @CrossOrigin("*")
    @ResponseBody
    public HashMap<String, Object> simphonyChecks(Principal principal,
                                                        @RequestPart(name = "startDate", required = false) String startDate,
                                                        @RequestPart(name = "endDate", required = false) String endDate,
                                                        @RequestPart(name="cardNumber", required = false) String cardNumber){

        HashMap<String, Object> response = new HashMap<>();

        try {
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                if(cardNumber == null){
                    cardNumber = "";
                }

                if((startDate == null || startDate.equals("") || startDate == null || endDate.equals("")) && (cardNumber == null || cardNumber.equals(""))){
                    response = simphonyPaymentService.findAllByAccountIdAndDeleted(account.getId(), false);
                    return response;
                } else{
                    response = simphonyPaymentService.getCheckPayment(startDate, endDate, account);
                    return response;
                }
            }else {
                return response;
            }

        }catch (Exception ex){
            ex.printStackTrace();
            return response;
        }
    }

}

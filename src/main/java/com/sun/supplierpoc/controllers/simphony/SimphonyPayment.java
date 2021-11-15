package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.simphony.request.SimphonyPaymentReq;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.SimphonyPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController("/simphonyPayment")
public class SimphonyPayment{

    @Autowired
    private SimphonyPaymentService simphonyPaymentService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/payUsingPayMob")
    public ResponseEntity<?> paymentTransaction(@RequestHeader("Authorization") String authorization,
                                                @Valid @RequestBody SimphonyPaymentReq simphonyPayment,
                                                BindingResult result) {
        Response response = new Response();

        try{
            if(!result.hasErrors()) {
                InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
                Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());
                if(accountOptional.isPresent()){
                    Account account = accountOptional.get();
                    response = simphonyPaymentService.createSimphonyPaymentTransaction(simphonyPayment, account);
                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.ACCOUNT_NOT_EXIST);
                }
            }else{
            response.setStatus(false);
            response.setMessage(result.getAllErrors().get(0).getDefaultMessage());
            }
        }catch(Exception e){
            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

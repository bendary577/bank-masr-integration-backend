package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyCheck;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentRes;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.SimphonyPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/test/simphonyPayment")
public class SimphonyPayment{

    @Autowired
    private SimphonyPaymentService simphonyPaymentService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/paySplitCheck")
    public ResponseEntity<?> paymentTransaction(@RequestHeader("Authorization") String authorization,
                                                @Valid @RequestBody SimphonyPaymentReq simphonyPayment,
                                                BindingResult result) {
        Response response = new Response();

        try{
            if(!result.hasErrors()) {

                InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

                if(invokerUser != null) {


                    Optional<Account> accountOptional = accountService.getAccountOptional(invokerUser.getAccountId());

                    if (accountOptional.isPresent()) {

                        Account account = accountOptional.get();
                        response = simphonyPaymentService.createSimphonyPaymentTransaction(simphonyPayment, account);

                    } else {
                        response.setStatus(false);
                        response.setMessage(Constants.ACCOUNT_NOT_EXIST);
                    }
                }else{

                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_USER);

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

    @PostMapping("/simphonyCheck")
    public ResponseEntity<?> getSimphonyPayment(Principal principal,
                                                @RequestPart(name = "startDate", required = false) String startDate,
                                                @RequestPart(name = "endDate", required = false) String endDate,
                                                @RequestPart(name="cardNumber", required = false) String cardNumber){

        HashMap hashMap = new HashMap();
       List<SimphonyCheck> simphonyChecks= simphonyPaymentService.getCheckPayment();
        hashMap.put("transactions", simphonyChecks);
        return new ResponseEntity<>(hashMap, HttpStatus.OK);

    }


}

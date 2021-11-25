package com.sun.supplierpoc.controllers.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.OperaTransaction;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyCheck;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentRes;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.repositories.opera.OperaTransactionRepo;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.simphony.SimphonyPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/simphonyPayment")
public class SimphonyPayment{

    @Autowired
    private SimphonyPaymentService simphonyPaymentService;

    @Autowired
    private InvokerUserService invokerUserService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private OperationTypeRepo operationTypeRepo;

    @Autowired
    private OperaTransactionRepo operaTransactionRepo;

    private Conversions conversions = new Conversions();
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;


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

//    @PostMapping("/simphonyCheck1")
//    public ResponseEntity<?> getSimphonyPayment(Principal principal,
//                                                @RequestPart(name = "startDate", required = false) String startDate,
//                                                @RequestPart(name = "endDate", required = false) String endDate,
//                                                @RequestPart(name="cardNumber", required = false) String cardNumber){
//
//        HashMap hashMap = new HashMap();
//       List<SimphonyCheck> simphonyChecks= simphonyPaymentService.getCheckPayment();
//        hashMap.put("transactions", simphonyChecks);
//        return new ResponseEntity<>(hashMap, HttpStatus.OK);
//
//    }

    @PostMapping(value = "/simphonyCheck")
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


//    @GetMapping(value = "/countOperaTransaction")
//    @ResponseBody
//    public ResponseEntity getOperaTransactionStat(Principal principal,
//                                                  @RequestParam(name = "startDate", required = false) String startDate,
//                                                  @RequestParam(name = "endDate", required = false) String endDate){
//        try {
//            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
//            if (accountOptional.isPresent()) {
//                Account account = accountOptional.get();
//
//                if(startDate == null || startDate.equals("") || endDate == null || endDate.equals("")){
//                    int failedTransactionCount =  operaTransactionRepo.countByAccountIdAndDeletedAndStatus(account.getId(),
//                            false, Constants.FAILED);
//
//                    int succeedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatus(account.getId(),
//                            false, Constants.SUCCESS);
//
//                    double totalAmount = 0;
//                    List<OperaTransaction> transactions = operaTransactionRepo.findAllByAccountIdAndDeleted(account.getId(), false);
//
//                    for (OperaTransaction trans : transactions) {
//                        if(trans.getStatus().equals(Constants.SUCCESS))
//                            totalAmount += trans.getAmount();
//                    }
//
//                    double finalTotalAmount = totalAmount;
//                    return ResponseEntity.status(HttpStatus.OK).body(
//                            new HashMap<String, Object>() {
//                                {
//                                    put("succeedTransactionCount", succeedTransactionCount);
//                                    put("failedTransactionCount", failedTransactionCount);
//                                    put("totalTransactionAmount", finalTotalAmount);
//                                    put("Date", LocalDateTime.now());
//                                }
//                            });
//                } else{
//                    Date start;
//                    Date end;
//
//                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                    start = df.parse(startDate);
//                    end = new Date(df.parse(endDate).getTime() + MILLIS_IN_A_DAY);
//
//                    int failedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatusAndCreationDateBetween(account.getId(),
//                            false, Constants.FAILED, start, end);
//
//                    int succeedTransactionCount = operaTransactionRepo.countByAccountIdAndDeletedAndStatusAndCreationDateBetween(account.getId(),
//                            false, Constants.SUCCESS, start, end);
//
//                    double totalAmount = 0;
//                    List<OperaTransaction> transactions = operaTransactionRepo.findAllByAccountIdAndDeletedAndCreationDateBetween(
//                            account.getId(), false, start, end);
//
//                    for (OperaTransaction trans : transactions) {
//                        totalAmount += trans.getAmount();
//                    }
//
//                    double finalTotalAmount = totalAmount;
//                    return ResponseEntity.status(HttpStatus.OK).body(
//                            new HashMap<String, Object>() {
//                                {
//                                    put("succeedTransactionCount", succeedTransactionCount);
//                                    put("failedTransactionCount", failedTransactionCount);
//                                    put("totalTransactionAmount", finalTotalAmount);
//                                    put("Date", LocalDateTime.now());
//                                }
//                            });
//                }
//
//            }else {
//                return ResponseEntity.status(HttpStatus.OK).body(
//                        new HashMap<String, Object>() {
//                            {
//                                put("succeedTransactionCount", 0);
//                                put("failedTransactionCount", 0);
//                                put("totalTransactionAmount", 0);
//                                put("Date", LocalDateTime.now());
//                            }
//                        });
//            }
//
//        }catch (Exception ex){
//            ex.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    new HashMap<String, Object>() {
//                        {
//                            put("succeedTransactionCount", 0);
//                            put("failedTransactionCount", 0);
//                            put("totalTransactionAmount", 0);
//                            put("Date", LocalDateTime.now());
//                        }
//                    });
//        }
//    }
}

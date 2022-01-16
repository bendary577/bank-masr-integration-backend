package com.sun.supplierpoc.controllers.application;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.requests.VoucherRequest;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.simphony.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/simphonyLoyalty/vouchers")
public class VoucherController {

        @Autowired
        private VoucherService voucherService;

        @Autowired
        private AccountService accountService;

        @GetMapping
        public ResponseEntity<?> getAllVoucher(@RequestParam("page") int page,
                                                @RequestParam("size") int size,
                                                Principal principal){
            Response response = new Response();
            try {
                User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
                if (user != null) {
                    Account account = accountService.getAccount(user.getAccountId());
                    if (account != null) {

                        response = voucherService.getAllVoucher(account);

                        if (response.isStatus()) {
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }

                    } else {
                        response.setMessage(Constants.INVALID_ACCOUNT);
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    response.setMessage("Can't Add New Voucher Due To.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }catch(Exception e){
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }
        @PostMapping("/add")
        public ResponseEntity<?> addVoucher(@Valid @RequestBody VoucherRequest voucherRequest,
                                                     BindingResult result, Principal principal){

            Response response = new Response();
            try {
                User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
                if (user != null) {
                    Account account = accountService.getAccount(user.getAccountId());
                    if (account != null) {
                        if (result.hasErrors()) {

                            Response finalResponse = response;
                            result.getAllErrors().forEach(error -> finalResponse.setMessage(error.getDefaultMessage()));
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }

                        response = voucherService.addVoucher(account, voucherRequest);


                        if (response.isStatus()) {
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }

                    } else {
                        response.setMessage(Constants.INVALID_ACCOUNT);
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    response.setMessage("Can't Add New Voucher Due To.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }catch(Exception e){
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }

        @PutMapping("/update")
        public ResponseEntity<?> updateVoucher(@Valid @RequestBody VoucherRequest voucherRequest,
                                                     BindingResult result, Principal principal){

            Response response = new Response();
            try {
                User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
                if (user != null) {
                    Account account = accountService.getAccount(user.getAccountId());
                    if (account != null) {
                        if (result.hasErrors()) {

                            Response finalResponse = response;
                            result.getAllErrors().forEach(error -> finalResponse.setMessage(error.getDefaultMessage()));
                            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                        }

                        response = voucherService.updateVoucher(account, voucherRequest);


                        if (response.isStatus()) {
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        } else {
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }

                    } else {
                        response.setMessage(Constants.INVALID_ACCOUNT);
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    response.setMessage("Can't Add New Voucher Due To.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }catch(Exception e){
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }

        @PutMapping("/markDeleted")
        public ResponseEntity<?> markVoucherDeleted(@Valid @RequestBody Voucher voucher,
                                                    Principal principal, BindingResult result){
            Response response = new Response();

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

            if(user != null){
                Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

                if(accountOptional.isPresent()){
                    Account account = accountOptional.get();

                    response = voucherService.markVoucherDeleted(account, voucher);

                    if(response.isStatus()){
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }else{
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                }else{
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_ACCOUNT);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }else{
                response.setStatus(false);
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        }


}

package com.sun.supplierpoc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.requests.ExportRequest;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private SupportService supportService;

    @PostMapping("/supportExportedFiles")
    public ResponseEntity<?> supportExportedFiles(@RequestBody ExportRequest exportRequest,
                                                  Principal principal){
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if(user != null){
            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());
            if(accountOptional.isPresent()){

                Account account = accountOptional.get();

                response = exportedFile(user, account, principal, exportRequest);
                if(response.isStatus()){
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }else {
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            }else{
                response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }else{
            response.setMessage(Constants.INVALID_USER);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }


    public Response exportedFile(User user, Account account, Principal principal, ExportRequest exportRequest){

        Response response = new Response();

        if(exportRequest.getCostCenters().isEmpty()){
            response.setMessage("Wrong store.");
        }else if(exportRequest.getEmail().equals("")){
            response.setMessage("Wrong email.");
        }else if(exportRequest.getSyncJobTypes().isEmpty()){
            response.setMessage("Wrong module");
        }else {

            List<SyncJobType> syncJobTypes = exportRequest.getSyncJobTypes();
            List<CostCenter> costCenters = exportRequest.getCostCenters();
            String email = exportRequest.getEmail();

            Date fromDate;
            Date toDate;

            try {
                fromDate = exportRequest.getDateRange().getStartDate();
                toDate = exportRequest.getDateRange().getEndDate();
            } catch(Exception e){
                response.setStatus(false);
                response.setMessage(e.getMessage());
                return response;
            }

            Date finalFromDate = fromDate;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    supportService.supportExportedFile(user, account, finalFromDate, toDate, costCenters,
                            email, syncJobTypes, principal);
                }
            }).start();

            response.setStatus(true);
            response.setMessage("Your Request has been sent successfully.");
            return response;
        }
        response.setStatus(false);
        return response;
    }
}

package com.sun.supplierpoc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
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
import java.util.Optional;

@RestController
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private SupportService supportService;

    @PostMapping("/supportExportedFiles")
    public ResponseEntity<?> supportExportedFiles(@RequestPart("dateRange") String dateRang,
                                                  @RequestPart("store") String store,
                                                  @RequestPart("email") String email,
                                                  @RequestPart("moduleId") String moduleId,
                                                  Principal principal){

        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if(user != null){
            Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());
            if(accountOptional.isPresent()){

                Account account = accountOptional.get();

                response = exportedFile(user, account, dateRang, store, email, moduleId);
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


    public Response exportedFile(User user, Account account, String StringDateRange, String store, String email, String moduleId){

        Response response = new Response();

        if(store.equals("")){
            response.setMessage("Wrong store.");
        }else if( email.equals("")){
            response.setMessage("Wrong email.");
        }else if(moduleId.equals("")){
            response.setMessage("Wrong module");
        }else {

            ObjectMapper objectMapper = new ObjectMapper();

            HashMap dateRange;
            Date fromDate;
            Date toDate;

            try {
                dateRange = objectMapper.readValue(StringDateRange,  new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                response.setStatus(false);
                response.setMessage(e.getMessage());
                return response;
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                fromDate = df.parse(dateRange.get("startDate").toString());
                fromDate = supportService.addDays(fromDate, 1);
                toDate = df.parse(dateRange.get("endDate").toString());
            } catch (ParseException e) {
                response.setStatus(false);
                response.setMessage(e.getMessage());
                return response;
            } catch(Exception e){
                response.setStatus(false);
                response.setMessage(e.getMessage());
                return response;
            }

            response = supportService.supportExportedFile(user, account, fromDate, toDate, store, email, moduleId);

            return response;
        }
        response.setStatus(false);
        return response;
    }
}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.FeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private AccountService accountService;

    @PostMapping("/addFeature")
    public ResponseEntity<?> addFeature(@RequestBody Feature featureRequest, Principal principal) {

        Response response = new Response();

        try {

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Account account = accountService.getAccount(user.getAccountId());

            if (account != null) {
                try {

                    Feature feature = featureService.save(featureRequest);

                    response.setData(feature);
                    return new ResponseEntity<>(response, HttpStatus.OK);

                } catch (Exception e) {

                    response.setMessage(e.getMessage());
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

                }

            } else {
                response.setMessage(Constants.INVALID_USER);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/getFeature")
//    public ResponseEntity<?> featureRequest(@RequestPart("id") String id, Principal principal){
//
//        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
//
//    }
}

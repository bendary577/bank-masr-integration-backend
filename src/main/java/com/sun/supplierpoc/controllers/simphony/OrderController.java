package com.sun.supplierpoc.controllers.simphony;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Optional;

@RestController

public class OrderController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/GetSimphonyCheckDetail")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> GetSimphonyCheckDetailRequest(Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = GetSimphonyCheckDetail(user.getId(), account);
            if(!response.isStatus()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        String message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public Response GetSimphonyCheckDetail(String userId, Account account){
        Response response = new Response();

        return response;
    }
}
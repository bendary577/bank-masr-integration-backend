package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.opera.OccupancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
public class OccupancyController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private OccupancyService occupancyService;
    @Autowired
    private InvokerUserService invokerUserService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/opera/occupancyUpdate")
    public ResponseEntity occupancyUpdate(@RequestParam(value = "roomsAvailable") String roomsAvailable,
                           @RequestParam(value = "roomsOccupied") String roomsOccupied,
                           @RequestParam(value = "roomsOnMaintenance") String roomsOnMaintenance,
                           @RequestParam(value = "roomsBooked") String roomsBooked,
                                     @RequestHeader("Authorization") String authorization
    ) {
        String message = "";
        Response response = new Response();

        InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);

        if(invokerUser != null) {
            Optional<Account> accountOptional = accountRepo.findById(invokerUser.getAccountId());

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                try {
                    SyncJobData syncJobData = occupancyService.createOccupancyObject(roomsAvailable, roomsOccupied,
                            roomsOnMaintenance, roomsBooked);
                    response = occupancyService.fetchOccupancyFromReport(invokerUser.getId(), account, syncJobData);

                    if(response.isStatus()){
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                } catch (Exception e) {
                    message = "Could not fetch occupancy Updates.";
                    response.setMessage(message);
                    response.setStatus(false);

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }else{
                message = "Invalid Account";
                response.setMessage(message);
                response.setStatus(false);

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

        }else{
            message = "Invalid Credentials";
            response.setMessage(message);
            response.setStatus(false);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @RequestMapping("/fetchOccupancyUpdate")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity fetchOccupancyUpdate(Principal principal) {
        String message = "";
        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            try {
                response = occupancyService.fetchOccupancyFromReport(user.getId(), account, null);

                if(response.isStatus()){
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } catch (Exception e) {
                message = "Could not fetch occupancy Updates.";
                response.setMessage(message);
                response.setStatus(false);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}

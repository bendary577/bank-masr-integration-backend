package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.booking.ReservationRow;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.opera.BookingService;
import com.sun.supplierpoc.services.opera.CancelBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RestController
public class CancelBookingController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private CancelBookingService cancelBookingService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private InvokerUserService invokerUserService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/opera/cancelBooking")
    public ResponseEntity cancelBooking(
            @RequestBody ReservationRow reservation,
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
                    /* Prepare Sync Object */
                    SyncJobData syncJobData = null;

                    if(syncJobData != null){
                        response = cancelBookingService.fetchCancelBookingFromReport(invokerUser.getId(), account);
                        if(response.isStatus()){
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        }else {
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                        }
                    }
                    response.setMessage("Neglected reservation");
                    response.setStatus(true);
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } catch (Exception e) {
                    e.printStackTrace();
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


    @RequestMapping("/fetchCancelBooking")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity fetchCancelBooking(Principal principal) {
        String message = "";
        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            try {
                response = cancelBookingService.fetchCancelBookingFromReport(user.getId(), account);

                if(response.isStatus()){
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } catch (Exception e) {
                message = "Could not fetch cancel booking entries.";
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

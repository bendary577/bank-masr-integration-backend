package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.opera.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController

public class BookingController {
    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    private BookingService bookingService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/fetchNewBooking")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity fetchNewBooking(Principal principal) {
        String message = "";
        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            SyncJobType newBookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);

            BookingConfiguration bookingConfiguration = newBookingSyncType.getConfiguration().bookingConfiguration;

            try {
                response = bookingService.fetchNewBookingFromReport(user.getId(), account, bookingConfiguration);

                if(response.isStatus()){
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            } catch (Exception e) {
                message = "Could not fetch new booking entries.";
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
            SyncJobType cancelBookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);
            BookingConfiguration bookingConfiguration = cancelBookingSyncType.getConfiguration().bookingConfiguration;

            try {
                response = bookingService.fetchCancelBookingFromReport(user.getId(), account, bookingConfiguration);

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
            SyncJobType occupancyUpdateSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.OCCUPANCY_UPDATE_REPORT, account.getId(), false);
            BookingConfiguration bookingConfiguration = occupancyUpdateSyncType.getConfiguration().bookingConfiguration;

            try {
                response = bookingService.fetchOccupancyFromReport(user.getId(), account, bookingConfiguration);

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

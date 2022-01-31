package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.booking.Reservation;
import com.sun.supplierpoc.models.opera.booking.ReservationRow;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.opera.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@RestController

public class BookingController {
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private InvokerUserService invokerUserService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/opera/uploadFile")
    public String uploadFile(@RequestParam(value = "accountName") String accountName,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        if (file != null) {
            if(accountName == null || accountName.equals("")){
                return "Account name can not be empty, Please try again";
            }else {
                try {
                    String bucketPath = accountName;
                    String fileName = "";

                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                    Date date = new Date();

                    if(Objects.requireNonNull(file.getOriginalFilename()).contains("cancel_booking")){
                        bucketPath += "/CancelBooking/";
                        fileName = "CancelBooking";
                    } else if(file.getOriginalFilename().contains("booking")){
                        bucketPath += "/Booking/";
                        fileName = "Booking";
                    } else if(file.getOriginalFilename().contains("occupancy")){
                        bucketPath += "/Occupancy/";
                        fileName = "Occupancy";
                    } else if(file.getOriginalFilename().contains("expenses")){
                        bucketPath += "/Expenses/";
                        fileName = "Expenses";
                    }

                    fileName += (dateFormat.format(date) + ".xml");

                    if((bucketPath.equals("") || fileName.equals(""))){
                        return "Failed to upload file, please try again.";
                    } else{
                        String fileURL = imageService.storeFile(file, bucketPath, fileName);
                        System.out.println(fileURL);
                    }
                    return "File uploaded successfully.";
                } catch (Exception e) {
                    return "Failed to upload file, please try again.";
                }
            }
        }
        else{
            return "File can not be empty, Please try again";
        }
    }

    @PostMapping("/opera/newBooking")
    public ResponseEntity newBooking(
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
                    SyncJobData syncJobData = bookingService.createBookingNewObject(reservation, account);
                    if(syncJobData != null){
                        response = bookingService.fetchNewBookingFromReport(invokerUser.getId(), account, syncJobData);
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

            try {
                response = bookingService.fetchNewBookingFromReport(user.getId(), account, null);

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
}

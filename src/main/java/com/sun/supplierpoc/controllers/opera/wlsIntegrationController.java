package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.Reservation;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.ReservationService;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reservation")
public class wlsIntegrationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/syncExcel")
    public ResponseEntity<?> uploadFile() {

        //@RequestParam("file") MultipartFile file,

        String message = "";

//        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        User user = userRepo.findByUsername("adminReservation");
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

//            if (ExcelHelper.hasExcelFormat(file)) {
//                try {
//                    Response response = reservationService.syncReservation(user.getId(), account, file);
//                    message = "Uploaded the file successfully: " + file.getOriginalFilename();
//                    response.setStatus(true);
//                    return ResponseEntity.status(HttpStatus.OK).body(message);
//                } catch (Exception e) {
//                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
//                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
//                }
//            }
        }
        message = "Please upload an excel file!";
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

}

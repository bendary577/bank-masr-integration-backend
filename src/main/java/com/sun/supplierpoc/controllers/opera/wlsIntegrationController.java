package com.sun.supplierpoc.controllers.opera;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.Item;
import com.sun.supplierpoc.models.opera.Reservation;
import com.sun.supplierpoc.models.opera.Transaction;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.services.ReservationService;
import com.sun.supplierpoc.services.opera.TransWebServ;
import org.apache.tomcat.util.http.parser.Authorization;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
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

    @Autowired
    private TransWebServ transWebServ;

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

    @RequestMapping("/transaction")
    public Transaction transaction(@RequestBody Transaction transaction) throws IOException {

        return transaction;
    }

    @RequestMapping("/getTransaction")
    public Transaction getTransaction(){
        List<Item> items = new ArrayList<Item>();
        Transaction transaction = new Transaction("A", "B", "G", "C" +
                items, "W", "r", "q", "e", "g");
        Transaction transaction1 = transWebServ.transactionService(transaction);

    return transaction1;
    }
}

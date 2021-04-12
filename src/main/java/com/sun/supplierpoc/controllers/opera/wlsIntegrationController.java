package com.sun.supplierpoc.controllers.opera;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.opera.Item;
import com.sun.supplierpoc.models.opera.Transaction;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.services.ReservationService;
import com.sun.supplierpoc.services.opera.TransWebServ;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/2wlsIntegration")
public class wlsIntegrationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AccountRepo accountRepo;

//    @Autowired
//    private TransWebServ transWebServ;

    @RequestMapping("/syncExcel")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<?> uploadFile(Principal principal) throws IOException {

        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            try {
                response = reservationService.syncReservation(user.getId(), account);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } catch (Exception e) {
                response.setStatus(false);
                response.setMessage("Could not upload the file");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(response);
            }
        }
        response.setMessage("Please upload an excel file!");
        response.setStatus(false);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @RequestMapping("/getTransaction")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Transaction getTransaction(Principal principal) {
        Item item = new Item("9", "34A", "10", "1120.22");
        List<Item> items = new ArrayList<Item>();
        items.add(item);
        Transaction transaction = new Transaction("1001", "2041 2578 3654 9876", "Gaad2", "110",
                "online", "10/3/2021", "MC", "Resa/checkout", "OTA/Direct", items);
//        Transaction transaction1 = transWebServ.transactionService(transaction);
        Transaction transaction1 = new Transaction();
        return transaction1;
    }
}

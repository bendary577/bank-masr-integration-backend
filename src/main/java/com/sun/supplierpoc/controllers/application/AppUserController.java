package com.sun.supplierpoc.controllers.application;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Company;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;


@RestController

public class AppUserController {
    @Autowired
    ApplicationUserRepo userRepo;
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    QRCodeGenerator qrCodeGenerator;
    @Autowired
    SendEmailService emailService;

    private static final String QR_CODE_IMAGE_PATH = "./src/main/resources/QRCode.png";
    private static final String LOGO_IMAGE_PATH = "./src/main/resources/logo.png";

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationUsers(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            ArrayList<ApplicationUser> companies = userRepo.findAll();
            return  ResponseEntity.status(HttpStatus.OK).body(companies);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationUser(@RequestParam(name = "addFlag") boolean addFlag,
                                                @RequestBody ApplicationUser applicationUser, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            if(addFlag){
                applicationUser.setCreationDate(new Date());
                applicationUser.setLastUpdate(new Date());
                applicationUser.setDeleted(false);
            }else {
                applicationUser.setLastUpdate(new Date());
            }

            userRepo.save(applicationUser);
            return ResponseEntity.status(HttpStatus.OK).body(applicationUser);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }


    @GetMapping(path = "/Simphony/sendQRCodeEmail")
    public void sendQRCodeEmail(){
        try {
            emailService.sendMimeMail(QR_CODE_IMAGE_PATH, LOGO_IMAGE_PATH);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    @GetMapping(path = "/generateAndDownloadQRCode")
    public void generateAndDownloadQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        qrCodeGenerator.generateQRCodeImage(codeText, width, height, QR_CODE_IMAGE_PATH);
    }

    @GetMapping(value = "/Simphony/generateQRCode")
    public ResponseEntity<byte[]> generateQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(qrCodeGenerator.getQRCodeImage(codeText, width, height));
    }
}

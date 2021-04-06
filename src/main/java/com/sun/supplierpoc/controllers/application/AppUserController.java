package com.sun.supplierpoc.controllers.application;
import com.google.zxing.WriterException;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import com.sun.supplierpoc.services.application.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController

public class    AppUserController {
    @Autowired
    ApplicationUserRepo userRepo;
    @Autowired
    AccountRepo accountRepo;
    @Autowired
    QRCodeGenerator qrCodeGenerator;
    @Autowired
    SendEmailService emailService;
    @Autowired
    private AppUserService appUserService;

    @Autowired
    private ImageService imageService;

//    private static final String QR_CODE_IMAGE_PATH = "./src/main/resources/QRCode.png";
//    private static final String LOGO_IMAGE_PATH = "./src/main/resources/logo.png";

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationUsers(Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<ApplicationUser> applicationUsers = userRepo.findAllByAccountId(account.getId());
            return  ResponseEntity.status(HttpStatus.OK).body(applicationUsers);
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
                applicationUser.setAccountId(account.getId());
                applicationUser.setCreationDate(new Date());
                applicationUser.setLastUpdate(new Date());
                applicationUser.setDeleted(false);
            }else {
                applicationUser.setLastUpdate(new Date());
            }

            Random random = new Random();
            String code = applicationUser.getName() +random.nextInt();
            String logoPath = "https://storage.googleapis.com/oracle-integrator-bucket/logo.png-1856061613?GoogleAccessId=accour@oracle-symphony-integrator.iam.gserviceaccount.com&Expires=1617796802&Signature=oAY183ycuF6%2F3%2FPNJZ64znzCKyx6tjEP5p3GSnkhG4qY%2Bakn%2FSOusi0wQktp6EMXHXyEFY3NeWzxTPe5xB%2F0KYNx1lq6HnsKie%2FQQBxLbFwSWjNMb3PM3bm712z1PFuEnmXnFV3P2fo8iwvhbNcnl%2BHa6lmSJalCosyarVfXmHH9QdqLYLYcZ4k%2BzswYhtwwyKx%2BoZUkB4Ca10JlrkXMJz3Qb8T2rZ2kUjpf05jJQtsQ6XC4TrU5cWsnKQbvH1Gj1Ib%2BUXXVQ5geKZbSMLr9o2R9Fdsg4QhgXJ0qtZCNUhR6J8hHXLs455AEkc8zR24f2yYLiPLsKrL0kujGI2aJ4Q%3D%3D";
            String QRPath = code +".png" ;

            if(account.getImageUrl() != null) {
                logoPath = account.getImageUrl();
            }

            applicationUser.setCode(code);

            try {
                String QrPath = qrCodeGenerator.getQRCodeImage(code,200, 200, QRPath);
                emailService.sendMimeMail(QrPath, logoPath, applicationUser);
            } catch (WriterException | IOException e) {
                return ResponseEntity.status(HttpStatus.OK).body(e.getMessage());
            }

            userRepo.save(applicationUser);

            return ResponseEntity.status(HttpStatus.OK).body(applicationUser);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationUserImage")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationGroupImage(@RequestPart(name = "userId", required = false) String groupId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            Optional<ApplicationUser> applicationUserOptional = userRepo.findById(groupId);


            if (applicationUserOptional.isPresent()) {

                ApplicationUser applicationUser = applicationUserOptional.get();

                String logoUrl = imageService.store(image);

                applicationUser.setLogoUrl(logoUrl);
                userRepo.save(applicationUser);

            }
            return ResponseEntity.status(HttpStatus.OK).body(applicationUserOptional);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/deleteApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationUsers(@RequestBody List<ApplicationUser> applicationUsers, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (ApplicationUser applicationUser : applicationUsers) {
                applicationUser.setDeleted(true);
                userRepo.save(applicationUser);
            }
            return ResponseEntity.status(HttpStatus.OK).body(applicationUsers);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/getTopUser")
    public List getTransactionByType(Principal principal) {

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            List<ApplicationUser> applicationUsers = appUserService.getTopUsers(account);

            return applicationUsers;
        }else{
            return new ArrayList<>();
        }
    }

//    @GetMapping(path = "/Simphony/sendQRCodeEmail")
//    public void sendQRCodeEmail(ApplicationUser user){
//        try {
//            emailService.sendMimeMail(QR_CODE_IMAGE_PATH, LOGO_IMAGE_PATH,user);
//        } catch (MailException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @GetMapping(path = "/generateAndDownloadQRCode")
//    public void generateAndDownloadQRCode(
//            @RequestParam(name = "codeText") String codeText,
//            @RequestParam(name = "width") Integer width,
//            @RequestParam(name = "height") Integer height)
//            throws Exception {
//        qrCodeGenerator.generateQRCodeImage(codeText, width, height, QR_CODE_IMAGE_PATH);
//    }

//    @GetMapping(value = "/Simphony/generateQRCode")
//    public ResponseEntity<byte[]> generateQRCode(
//            @RequestParam(name = "codeText") String codeText,
//            @RequestParam(name = "width") Integer width,
//            @RequestParam(name = "height") Integer height)
//            throws Exception {
//        return ResponseEntity.status(HttpStatus.OK).body(qrCodeGenerator.getQRCodeImage(codeText, width, height));
//    }
}

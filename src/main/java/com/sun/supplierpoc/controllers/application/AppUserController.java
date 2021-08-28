package com.sun.supplierpoc.controllers.application;

import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SmsPojo;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import com.sun.supplierpoc.services.SmsService;
import com.sun.supplierpoc.services.application.AppUserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
public class AppUserController {


    @Autowired ApplicationUserRepo userRepo;

    @Autowired AccountRepo accountRepo;

    @Autowired QRCodeGenerator qrCodeGenerator;

    @Autowired SendEmailService emailService;

    @Autowired private AppUserService appUserService;
    @Autowired private ImageService imageService;
    @Autowired private GroupRepo groupRepo;
    @Autowired private GeneralSettingsRepo generalSettingsRepo;


    @Autowired
    SmsService service;

    @Autowired
    private SimpMessagingTemplate webSocket;

    private final String  TOPIC_DESTINATION = "/lesson/sms";


    @RequestMapping("/getApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationUsers(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<ApplicationUser> applicationUsers = userRepo.findAllByAccountId(account.getId());
            return ResponseEntity.status(HttpStatus.OK).body(applicationUsers);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationGroupImage(@RequestParam(name = "addFlag", required = true) boolean addFlag,
                                                   @RequestParam(name = "isGeneric", required = true) boolean isGeneric,
                                                   @RequestPart(name = "name" , required = false) String name,
                                                   @RequestPart(name = "cardCode" , required = true) String cardCode,
                                                   @RequestPart(name = "email", required = false) String email,
                                                   @RequestPart(name = "groupId", required = false) String groupId,
                                                   @RequestPart(name = "userId", required = false) String userId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   @RequestParam(name = "mobile", required = false) String mobile,
                                                   @RequestParam(name = "balance", required = false) String balance,
                                                   @RequestParam(name = "expire", required = false) double expire,
                                                   @RequestPart(name="accompaniedGuests", required = false) String accompaniedGuests,
                                                   Principal principal) {

        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if(user != null ) {
            try {

                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                    response = appUserService
                            .addUpdateGuest(addFlag, isGeneric, name, email, groupId, userId,
                                    image, account, generalSettings, accompaniedGuests, balance, cardCode, expire, mobile);

                    if ((Boolean) response.get("success")) {
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }


                } else {
                    response.put("message", "Invalid user.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.put("message", "Something went wrong.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }else{
            response.put("message", Constants.INVALID_USER);
            return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "/resendQRCode")
    public ResponseEntity resendQRCode(@RequestPart(name = "userId", required = false) String userId,
                                       Principal principal) {
        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            Optional<ApplicationUser> applicationUser = userRepo.findById(userId);

            if(applicationUser.isPresent()) {

                ApplicationUser appUser = applicationUser.get();

                Optional<Group> groupOptional = groupRepo.findById(appUser.getGroup().getId());
                if(groupOptional.isPresent()) {

                    Group group = groupOptional.get();

                    try {

                        Random random = new Random();
                        String code = appUser.getEmail().substring(0, appUser.getEmail().indexOf('@')) + random.nextInt(100);
                        String QRPath = "QRCodes/" + code + ".png";
                        String accountLogo = account.getImageUrl();
                        String mailSubj = generalSettings.getMailSub();

                        String QrPath = qrCodeGenerator.getQRCodeImage(code, 200, 200, QRPath);
                        appUser.setCode(code);
                        if (emailService.sendMimeMail(QrPath, accountLogo, mailSubj, account.getName(), appUser, account)) {
                            userRepo.save(appUser);

                            response.put("message", "QRCode send successfully.");
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        } else {
                            response.put("message", "Invalid user email.");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        }
                    } catch (WriterException | IOException e) {
                        LoggerFactory.getLogger(ApplicationUser.class).info(e.getMessage());
                        response.put("message", "Invalid user email.");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                    }
                }else{
                    response.put("message", "User group not found.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }else{
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            response.put("message", "Invalid user.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    @PostMapping(path = "/sendWelcomeMail")
    public ResponseEntity sendWelcomeMail(@RequestParam(name = "userId") String userId,
                                            Principal principal) {
        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            Optional<ApplicationUser> applicationUser = userRepo.findById(userId);

            if(applicationUser.isPresent()) {

                ApplicationUser appUser = applicationUser.get();

                Optional<Group> groupOptional = groupRepo.findById(appUser.getGroup().getId());
                if(groupOptional.isPresent()) {
                    Group group = groupOptional.get();
                    try {
                        String accountLogo = account.getImageUrl();
                        String mailSubj = generalSettings.getMailSub();
                        if (emailService.sendWelcomeEmail(accountLogo, mailSubj, account.getName(), appUser, account)) {
                            response.put("message", "Mail send successfully.");
                            return ResponseEntity.status(HttpStatus.OK).body(response);
                        } else {
                            response.put("message", "Invalid user email.");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        }
                    } catch (Exception e) {
                        LoggerFactory.getLogger(ApplicationUser.class).info(e.getMessage());
                        response.put("message", "Invalid user email.");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                    }
                }else{
                    response.put("message", "User group not found.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }else{
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            response.put("message", "Invalid user.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }



    @RequestMapping("/deleteApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationUsers(@RequestParam(name = "addFlag") boolean addFlag,
                                                 @RequestBody List<ApplicationUser> applicationUsers, Principal principal) {

        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (ApplicationUser applicationUser : applicationUsers) {

                Optional<Group> groupOptional  = groupRepo.findById(applicationUser.getGroup().getId());

                if(groupOptional.isPresent()) {
                    Group group = groupOptional.get();

                    if(!group.isDeleted()) {
                        applicationUser.setDeleted(addFlag);
                        userRepo.save(applicationUser);
                    }else{
                        response.put("message", "The group of the user "+applicationUser.getName() +" is already deleted,\n try to update his group.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }else{
                    response.put("message", "The group of the user "+applicationUser.getName() +" is already deleted, \n try to update his group.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            if(addFlag) {
                response.put("message", "Deleted Successfully.");
            }else{
                response.put("message", "Restored Successfully.");
            }
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/getTopUser")
    public List getTransactionByType(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            List<ApplicationUser> applicationUsers = appUserService.getTopUsers(account);

            return applicationUsers;
        } else {
            return new ArrayList<>();
        }
    }

    @GetMapping(path = "/generateAndDownloadQRCode")
    public void generateAndDownloadQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        qrCodeGenerator.getQRCodeImage(codeText, width, height, "C:\\Users\\basse\\.Bassel Work Space\\infor-sun-poc\\src\\main\\resources\\Qr.png");
    }

    @GetMapping("filterGuest")
    public ResponseEntity<?> filterGuests(@RequestParam("name") String name,
                                          @RequestParam("creationDateStart") String fromDate,
                                          @RequestParam("creationDateEnd") String toDate,
                                          @RequestParam("cardNumber") String cardNumber,
                                          @RequestParam("cardStatues") String cardStatues,
                                          @RequestParam("groupId") String groupId,
                                          Principal principal){

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if(user != null){

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if(accountOptional != null){

                List<ApplicationUser> applicationUsers = appUserService.filterByParameters(name, fromDate, toDate,
                        cardNumber, cardStatues, groupId);


            }else{

            }

        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    @RequestMapping(value = "/sms", method = RequestMethod.POST)
    public void smsSubmit(@RequestBody SmsPojo sms) {
        LoggerFactory.getLogger("new T");
        try{
            service.send(sms);
        }
        catch(Exception e){

//            webSocket.convertAndSend(TOPIC_DESTINATION, getTimeStamp() + ": Error sending the SMS: "+e.getMessage());
            throw e;
        }
        webSocket.convertAndSend(TOPIC_DESTINATION,  ": SMS has been sent!: "+sms.getTo());

    }

//    @GetMapping(value = "/Simphony/generateQRCode")
//    public ResponseEntity<byte[]> generateQRCode(
//            @RequestParam(name = "codeText") String codeText,
//            @RequestParam(name = "width") Integer width,
//            @RequestParam(name = "height") Integer height)
//            throws Exception {
//        return ResponseEntity.status(HttpStatus.OK).body(qrCodeGenerator.getQRCodeImage(codeText, width, height));
//    }

//    @GetMapping(path = "/Simphony/sendQRCodeEmail")
//    public void sendQRCodeEmail(ApplicationUser user){
//        try {
//            emailService.sendSimpleMail();
//        } catch (MailException e) {
//            e.printStackTrace();
//        }
//    }
}

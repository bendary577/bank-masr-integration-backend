package com.sun.supplierpoc.controllers.application;
import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.applications.   ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import com.sun.supplierpoc.services.application.AppUserService;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private GroupRepo groupRepo;

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
    public ResponseEntity addApplicationGroupImage(@RequestParam(name = "addFlag") boolean addFlag,
                                                   @RequestPart(name = "name", required = false) String name,
                                                   @RequestPart(name = "email", required = false) String email,
                                                   @RequestPart(name = "groupId") String groupId,
                                                   @RequestPart(name = "userId", required = false) String userId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   Principal principal) {

        HashMap response = new HashMap();
        try {

            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                ApplicationUser applicationUser;
                Group group ;

                if (addFlag) {

                    applicationUser = new ApplicationUser();

                    if (groupId != null) {
                        Optional<Group> groupOptional = groupRepo.findById(groupId);
                        if (groupOptional.get() == null) {
                            response.put("message", "Group doesn't exist");
                            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                        }
                        group = groupOptional.get();
                        applicationUser.setGroup(group);
                    }else {
                        response.put("message", "Group can't be empty.");
                        return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                    }

                    String logoUrl = Constants.USER_IMAGE_URL;
                    if (image != null) {
                        try {
                            logoUrl = imageService.store(image);
                        } catch (Exception e) {
                            LoggerFactory.getLogger(GroupController.class).info(e.getMessage());
                        }
                    }

                    applicationUser.setName(name);
                    applicationUser.setEmail(email);
                    applicationUser.setLogoUrl(logoUrl);
                    applicationUser.setAccountId(account.getId());
                    applicationUser.setCreationDate(new Date());
                    applicationUser.setLastUpdate(new Date());
                    applicationUser.setDeleted(false);

                    Random random = new Random();
                    String code = applicationUser.getName() +random.nextInt();
                    String logoPath = account.getImageUrl();
                    String QRPath = "./src/main/resources/"+ code +".png" ;
                    applicationUser.setCode(code);

                    try {
                        String QrPath = qrCodeGenerator.getQRCodeImage(code,200, 200, QRPath);
                        emailService.sendMimeMail(QrPath, logoPath, applicationUser);
                    } catch (WriterException | IOException e) {
                        LoggerFactory.getLogger(ApplicationUser.class).info(e.getMessage());
                    }

                    userRepo.save(applicationUser);

                } else {

                    Optional<ApplicationUser> userOptional = userRepo.findById(userId);

                    if (userOptional.isPresent()) {

                        applicationUser = userOptional.get();

                        if (groupId != null) {
                            Optional<Group> groupOptional = groupRepo.findById(groupId);
                            if (groupOptional.get().getParentGroup() == null) {
                                response.put("message", "Group doesn't exist");
                                return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                            }
                            group = groupOptional.get();
                            applicationUser.setGroup(group);
                        } else {
                            response.put("message", "Group can't be empty.");
                            return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                        }

                        String logoUrl = Constants.USER_IMAGE_URL;
                        if (image != null) {
                            try {
                                logoUrl = imageService.store(image);
                            } catch (Exception e) {
                                LoggerFactory.getLogger(GroupController.class).info(e.getMessage());
                            }
                        }

                        applicationUser.setName(name);
                        applicationUser.setEmail(email);
                        applicationUser.setLogoUrl(logoUrl);
                        applicationUser.setAccountId(account.getId());
                        group.setLastUpdate(new Date());
                        userRepo.save(applicationUser);
                    }
                }
                response.put("message", "User saved successfully.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("message", "Invalid user.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }catch (Exception e){
            response.put("message", "Something went wrong.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @RequestMapping("/deleteApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteApplicationUsers(@RequestParam(name = "addFlag") boolean addFlag,
                                                 @RequestBody List<ApplicationUser> applicationUsers, Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (ApplicationUser applicationUser : applicationUsers) {
                if(addFlag) {
                    applicationUser.setDeleted(true);
                }else{
                    applicationUser.setDeleted(false);
                }
                userRepo.save(applicationUser);
            }
            return ResponseEntity.status(HttpStatus.OK).body(applicationUsers);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/deleteAllUsersDeeply")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity deleteAllUsersDeeply(Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {

            userRepo.deleteAll();

            return ResponseEntity.status(HttpStatus.OK).body("Deleted");
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

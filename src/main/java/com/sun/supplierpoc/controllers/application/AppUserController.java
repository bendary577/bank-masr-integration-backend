package com.sun.supplierpoc.controllers.application;

import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
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

public class AppUserController {
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
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
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

                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                Group group;
                ApplicationUser applicationUser;

                if (addFlag) {
                    applicationUser = new ApplicationUser();

                    if (groupId != null) {
                         Optional<Group> groupOptional = groupRepo.findById(groupId);
                        if (groupOptional.isEmpty()) {
                            response.put("message", "User group doesn't exist");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                        group = groupOptional.get();
                        applicationUser.setGroup(group);
                    } else {
                        response.put("message", "User group can't be empty.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }

                    ApplicationUser oldApplicationUser = userRepo.findFirstByEmailAndAccountId(email, account.getId());
                    if (oldApplicationUser != null) {
                        response.put("message", "There is user exist with this email.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }

                    String logoUrl = Constants.USER_IMAGE_URL;
                    if (image != null) {
                        try {
                            logoUrl = imageService.store(image);
                        } catch (Exception e) {
                            response.put("message", "Can't save image.");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                        }
                    }

                    applicationUser.setName(name);
                    applicationUser.setEmail(email);
                    applicationUser.setLogoUrl(logoUrl);
                    applicationUser.setAccountId(account.getId());
                    applicationUser.setCreationDate(new Date());
                    applicationUser.setDeleted(false);

//                    String codeBuild = applicationUser.getName() + " " + group.getName() + " " + new Date().toString();
//                    Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
//                    byte[] encodedBytes = Base64.getEncoder().encode(codeBuild.getBytes());
//                    String code = new String(encodedBytes);

                    Random random = new Random();
                    String code = applicationUser.getEmail().substring(0, applicationUser.getEmail().indexOf('@')) + random.nextInt(100);

                    String accountLogo = account.getImageUrl();
                    String mailSubj = generalSettings.getMailSub();
                    String QRPath = "QRCodes/" + code + ".png";
                    applicationUser.setCode(code);

                    try {
                        String QrPath = qrCodeGenerator.getQRCodeImage(code, 200, 200, QRPath);
                        if (emailService.sendMimeMail(QrPath, accountLogo, mailSubj,account.getName(), applicationUser, account)) {
                            userRepo.save(applicationUser);
                            response.put("message", "User added successfully.");
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
                } else {
                    Optional<ApplicationUser> userOptional = userRepo.findById(userId);

                    if (userOptional.isPresent()) {
                        applicationUser = userOptional.get();

                        if (groupId != null) {
                            Optional<Group> groupOptional = groupRepo.findById(groupId);
                            if (groupOptional.isPresent()) {
                                group = groupOptional.get();
                                applicationUser.setGroup(group);
                            } else {
                                response.put("message", "Group doesn't exist");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                            }
                        } else {
                            response.put("message", "Group can't be empty.");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }

                        if (!applicationUser.getEmail().equals(email)) {
                            ApplicationUser oldApplicationUser = userRepo.findFirstByEmailAndAccountId(email, account.getId());

                            if (oldApplicationUser != null) {
                                response.put("message", "There is user exist with this email.");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                            }

                            String accountLogo = account.getImageUrl();
                            String mailSubj = generalSettings.getMailSub();
                            String QRPath = "QRCodes/" + applicationUser.getCode() + ".png";

                            try {
                                String QrPath = qrCodeGenerator.getQRCodeImage(applicationUser.getCode(), 200, 200, QRPath);
                                boolean emailStatus = emailService.sendMimeMail(QrPath, accountLogo, mailSubj, account.getName(), applicationUser, account);
                                if (!emailStatus) {
                                    response.put("message", "Invalid user email.");
                                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                                }
                            } catch (WriterException | IOException e) {
                                LoggerFactory.getLogger(ApplicationUser.class).info(e.getMessage());
                                response.put("message", "Invalid user email.");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                            }
                            applicationUser.setEmail(email);
                        }

                        if (image != null) {
                            String logoUrl = Constants.USER_IMAGE_URL;
                            try {
                                logoUrl = imageService.store(image);
                            } catch (Exception e) {
                                response.put("message", "Can't save image.");
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                            }
                            applicationUser.setLogoUrl(logoUrl);
                        }

                        applicationUser.setName(name);
                        applicationUser.setAccountId(account.getId());
                        group.setLastUpdate(new Date());
                        userRepo.save(applicationUser);

                        response.put("message", "User Updated successfully.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    } else {
                        response.put("message", "Can't find user with this id.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
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


    @GetMapping(path = "/generateAndDownloadQRCode")
    public void generateAndDownloadQRCode(
            @RequestParam(name = "codeText") String codeText,
            @RequestParam(name = "width") Integer width,
            @RequestParam(name = "height") Integer height)
            throws Exception {
        qrCodeGenerator.getQRCodeImage(codeText, width, height, "C:\\Users\\basse\\.Bassel Work Space\\infor-sun-poc\\src\\main\\resources\\Qr.png");
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

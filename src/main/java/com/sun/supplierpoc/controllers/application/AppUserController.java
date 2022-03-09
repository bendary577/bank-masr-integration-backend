package com.sun.supplierpoc.controllers.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.simphony.redeemVoucher.Voucher;
import com.sun.supplierpoc.pdfExporters.QRCodePDFGenerator;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.*;
import com.sun.supplierpoc.services.application.ActivityService;
import com.sun.supplierpoc.services.application.AppUserService;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
    private GroupRepo groupRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private AccountService accountService;
    @Autowired
    private SmsService smsService;
    @Autowired
    InvokerUserService invokerUserService;
    @Autowired
    FeatureService featureService;
    @Autowired
    ActivityService activityService;
    @Autowired
    QRCodePDFGenerator qrCodePDFGenerator;

    private final Conversions conversions = new Conversions();
    ///////////////////////////////////////////// Reward Points Program////////////////////////////////////////////////

    @RequestMapping("/rewardPoints/getGuestPoints")
    @CrossOrigin(origins = "*")
    public ResponseEntity getGuestPoints(@RequestHeader("Authorization") String authorization,
                                         @RequestParam String guestCode) {
        HashMap response = new HashMap();
        String username, password;
        try {
            final String[] values = conversions.convertBasicAuth(authorization);
            if (values.length != 0) {
                username = values[0];
                password = values[1];
                InvokerUser user = invokerUserService.getInvokerUser(username, password);
                if (user == null) {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "This user not allowed to access this method.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

                if (guestCode.equals("")) {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "Kindly provide the customer code.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                ApplicationUser applicationUser = userRepo.findByCodeAndAccountIdAndDeleted(guestCode, user.getAccountId(), false);
                if (applicationUser != null) {
                    if(applicationUser.isSuspended()){
                        response.put("isSuccess", false);
                        response.put("points", 0);
                        response.put("message", "This user has been suspended and is currently unable to benefit from the reward system.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                    response.put("isSuccess", true);
                    response.put("points", applicationUser.getPoints());
                    response.put("message", "The total number of points a user has is" + applicationUser.getPoints() + ".");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "This user is not a member of the reward points system.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response.put("isSuccess", false);
                response.put("points", 0);
                response.put("message", "This user not allowed to access this method.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

        } catch (Exception e) {
            response.put("isSuccess", false);
            response.put("points", 0);
            response.put("message", "There was a problem, please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RequestMapping("/rewardPoints/addGuestPoints")
    @CrossOrigin(origins = "*")
    public ResponseEntity addGuestPoints(@RequestHeader("Authorization") String authorization,
                                         @RequestParam(name = "guestCode") String guestCode,
                                         @RequestParam(name = "points") int points) {
        HashMap response = new HashMap();
        String username, password;
        try {
            final String[] values = conversions.convertBasicAuth(authorization);
            if (values.length != 0) {
                username = values[0];
                password = values[1];
                InvokerUser user = invokerUserService.getInvokerUser(username, password);
                if (user == null) {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "This user not allowed to access this method.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

                if (guestCode.equals("")) {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "Kindly provide the customer code.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                if(points <= 0){
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "Kindly provide valid points.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                ApplicationUser applicationUser = appUserService.getAppUserByCode(guestCode, user.getAccountId());
                if (applicationUser != null) {
                    if(applicationUser.isSuspended()){
                        response.put("isSuccess", false);
                        response.put("points", 0);
                        response.put("message", "This user has been suspended and is currently unable to benefit from the reward system.");
                        return ResponseEntity.status(HttpStatus.OK).body(response);
                    }

                    applicationUser.setPoints(applicationUser.getPoints() + points);
                    userRepo.save(applicationUser);

                    response.put("isSuccess", true);
                    response.put("points", applicationUser.getPoints());
                    response.put("message", "The total number of points a user has is" + applicationUser.getPoints() + ".");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("isSuccess", false);
                    response.put("points", 0);
                    response.put("message", "This user is not a member of the reward points system.");
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

            } else {
                response.put("isSuccess", false);
                response.put("points", 0);
                response.put("message", "This user not allowed to access this method.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

        } catch (Exception e) {
            response.put("isSuccess", false);
            response.put("points", 0);
            response.put("message", "There was a problem, please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @RequestMapping("/addRewardPointsUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addRewardPointsUser(@RequestPart(name = "user") String userJson,
                                              @RequestPart(name = "image", required = false) MultipartFile image,
                                              Principal principal) {
        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (user != null) {
            try {
                ApplicationUser applicationUser;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    applicationUser = objectMapper.readValue(userJson, new TypeReference<>() {});
                } catch (JsonProcessingException e) {
                    throw e;
                }
                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
                if (!accountOptional.isPresent()) {
                    response.put("message", Constants.INVALID_USER);
                    return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
                }

                Account account = accountOptional.get();
                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                response = appUserService
                        .addRewardPointsGuest(applicationUser, image,account, user, generalSettings);

            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Invalid user.");
            }

            return new ResponseEntity(response, HttpStatus.OK);
        }else{
            response.put("success", false);
            response.put("message", Constants.INVALID_USER);
            return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping("/updateRewardPointsUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity updateRewardPointsUser(@RequestPart(name = "user") String userJson,
                                              @RequestPart(name = "image", required = false) MultipartFile image,
                                              Principal principal) {
        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (user != null) {
            try {
                ApplicationUser applicationUser;
                ObjectMapper objectMapper = new ObjectMapper();
                applicationUser = objectMapper.readValue(userJson, new TypeReference<>() {});
                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isEmpty()) {
                    response.put("message", Constants.INVALID_USER);
                    return new ResponseEntity(Constants.INVALID_USER, HttpStatus.UNAUTHORIZED);
                }

                Account account = accountOptional.get();

                response = appUserService.updateRewardPointsGuest(applicationUser, image, account);

            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("message", "Invalid user.");
            }

            return new ResponseEntity(response, HttpStatus.OK);
        }else{
            response.put("success", false);
            response.put("message", Constants.INVALID_USER);
            return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/exportQRCodePDF")
    public void exportQRCodePDF(@RequestBody ArrayList<ApplicationUser> users,
                                 HttpServletResponse httpServletResponse,
                                 Principal principal) throws IOException {

        HashMap response = new HashMap();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();

            qrCodePDFGenerator.generatePdfReport(account, users, httpServletResponse);

            response.put("message", "Excel exported successfully.");
            LoggerFactory.getLogger(TransactionController.class).info(response.get("message").toString());
        }else{
            response.put("message", "Failed to export excel, Plese try again.");
        }
    }

    /////////////////////////////////////////////////////// *END* //////////////////////////////////////////////////////

    ////////////////////////////////////////////////// Entry System ////////////////////////////////////////////////////

    @PostMapping("/walletSystem/checkGuestBalance")
    public ResponseEntity<?> checkGuestBalance(@RequestHeader("Authorization") String authorization,
                                               @RequestBody String guestCode) {
        HashMap response = new HashMap();
        try {
            InvokerUser invokerUser = invokerUserService.getAuthenticatedUser(authorization);
            if (invokerUser == null) {
                response.put("isSuccess", false);
                response.put("balance", 0);
                response.put("message", "This user not allowed to access this method.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            if (guestCode.equals("")) {
                response.put("isSuccess", false);
                response.put("balance", 0);
                response.put("message", "Kindly provide the customer code.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            Account account = accountService.getAccount(invokerUser.getAccountId());

            if (account != null) {
                ApplicationUser applicationUser = appUserService.getAppUserByCode(guestCode, account.getId());

                if (applicationUser != null) {
                    double guestBalance = activityService.calculateBalance(applicationUser.getWallet());
                    guestBalance = conversions.roundUpDouble(guestBalance);
                    String message = "The guest with card number " + applicationUser.getCode() + " has a balance of "
                            + String.valueOf(guestBalance) + " " + account.getCurrency();
                    response.put("isSuccess", true);
                    response.put("balance", String.valueOf(guestBalance));
                    response.put("message", message);
                } else {
                    response.put("isSuccess", false);
                    response.put("balance", 0);
                    response.put("message", "This user is not a member of wallet system.");
                }

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("isSuccess", true);
                response.put("balance", 0);
                response.put("message", "This user is not a member of wallet system.");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();

            response.put("isSuccess", false);
            response.put("balance", 0);
            response.put("message", "This user is not a member of wallet system.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    /////////////////////////////////////////////////////// *END* //////////////////////////////////////////////////////

    @RequestMapping("/getApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationUsers(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            ArrayList<ApplicationUser> applicationUsers = appUserService.getAppUsersByAccountId(account.getId());

            return ResponseEntity.status(HttpStatus.OK).body(applicationUsers);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/applicationUsers/{id}")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getApplicationUsers(@PathVariable("id") String id, Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            Optional<ApplicationUser> applicationUserOptional = userRepo.findById(id);
            if (applicationUserOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.OK).body(applicationUserOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new ApplicationUser());
            }
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/addApplicationUser")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity addApplicationGroupImage(@RequestParam(name = "addFlag") boolean addFlag,
                                                   @RequestParam(name = "isGeneric") boolean isGeneric,
                                                   @RequestPart(name = "name", required = false) String name,
                                                   @RequestPart(name = "cardCode", required = true) String cardCode,
                                                   @RequestPart(name = "groupId", required = false) String groupId,
                                                   @RequestPart(name = "userId", required = false) String userId,
                                                   @RequestPart(name = "image", required = false) MultipartFile image,
                                                   @RequestParam(name = "mobile", required = false) String mobile,
                                                   @RequestPart(name = "email", required = false) String email,
                                                   @RequestParam(name = "balance", required = false) String balance,
                                                   @RequestParam(name = "expiryDate", required = false) String expiryDate,
                                                   @RequestParam(name = "sendEmail", required = false) boolean sendEmail,
                                                   @RequestParam(name = "sendSMS", required = false) boolean sendSMS,
                                                   @RequestPart(name = "accompaniedGuests", required = false) String accompaniedGuests,
                                                   Principal principal) {

        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        if (user != null) {
            try {

                Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

                if (accountOptional.isPresent()) {
                    Account account = accountOptional.get();

                    GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

                    response = appUserService
                            .addUpdateGuest(user, addFlag, isGeneric, name, email, groupId, userId, sendEmail, sendSMS,
                                    image, account, generalSettings, accompaniedGuests, balance, cardCode,
                                    expiryDate, mobile);

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
        } else {
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

            if (applicationUser.isPresent()) {

                ApplicationUser appUser = applicationUser.get();
                Optional<Group> groupOptional = groupRepo.findById(appUser.getGroup().getId());
                if (groupOptional.isPresent()) {

                    Group group = groupOptional.get();

                    try {
                        String code = createCode(appUser);
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
                } else {
                    response.put("message", "User group not found.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            } else {
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            response.put("message", "Invalid user.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }

    public String createCode(ApplicationUser appUser) {
        String code;
        try {
            Random random = new Random();
            code = appUser.getEmail().substring(0, appUser.getEmail().indexOf('@'));
            if (code.length() > 17) {
                code = code.substring(0, 17);
            } else {
                code = code + code.substring(0, 3);
            }
            code = (code + random.nextInt(1000)).replace(".", "");
            code = Base64UrlCodec.BASE64URL.encode(code);
        }catch (Exception e){
            throw new RuntimeException();
        }
        return code;
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

            if (applicationUser.isPresent()) {

                ApplicationUser appUser = applicationUser.get();

                Optional<Group> groupOptional = groupRepo.findById(appUser.getGroup().getId());
                if (groupOptional.isPresent()) {
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
                } else {
                    response.put("message", "User group not found.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            } else {
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
                                                 @RequestBody List<String> applicationUsers, Principal principal) {

        HashMap response = new HashMap();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            for (String applicationUserId : applicationUsers) {
                Optional<ApplicationUser> applicationUserObj = userRepo.findById(applicationUserId);
                ApplicationUser applicationUser;

                if (applicationUserObj.isPresent()) {
                    applicationUser = applicationUserObj.get();

                    Optional<Group> groupOptional = groupRepo.findById(applicationUser.getGroup().getId());

                    if (groupOptional.isPresent()) {
                        Group group = groupOptional.get();

                        if (!group.isDeleted()) {
                            applicationUser.setDeleted(addFlag);
                            applicationUser.setSuspended(addFlag);
                            userRepo.save(applicationUser);
                        } else {
                            response.put("message", "The group of the user " + applicationUser.getName() + " is already deleted,\n try to update his group.");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                        }
                    } else {
                        response.put("message", "The group of the user " + applicationUser.getName() + " is already deleted, \n try to update his group.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                } else
                    continue;
            }
            if (addFlag) {
                response.put("message", "Deleted Successfully.");
            } else {
                response.put("message", "Restored Successfully.");
            }
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return new ResponseEntity(HttpStatus.FORBIDDEN);
    }

    @RequestMapping("/suspendApplicationUsers")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity suspendApplicationUsers(@RequestParam(name = "susFlag") boolean susFlage,
                                                  @RequestParam(name = "userId") String userId,
                                                  Principal principal) {

        try {
            HashMap response = new HashMap();
            User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if (accountOptional.isPresent()) {

                Optional<ApplicationUser> tempApplicationUserOptional = userRepo.findById(userId);

                if (tempApplicationUserOptional.isPresent()) {
                    ApplicationUser applicationUser = tempApplicationUserOptional.get();
                    applicationUser.setSuspended(susFlage);
                    userRepo.save(applicationUser);

                    if (susFlage) {
                        response.put("message", "Suspended Successfully.");
                    } else {
                        response.put("message", "Actived Successfully.");
                    }
                    return ResponseEntity.status(HttpStatus.OK).body(response);

                } else {
                    response.put("message", "Guest dosen't exist.");
                    return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.FORBIDDEN);
        }
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
                                          Principal principal) {

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        if (user != null) {

            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

            if (accountOptional != null) {

                List<ApplicationUser> applicationUsers = appUserService.filterByParameters(name, fromDate, toDate,
                        cardNumber, cardStatues, groupId);


            } else {

            }

        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    @RequestMapping(value = "/sendSmsOrEmail", method = RequestMethod.POST)
    public ResponseEntity smsSubmit(@RequestParam("process") String process,
                                    @RequestBody ApplicationUser applicationUser) {
        LoggerFactory.getLogger("new T");
        try {
            if (process.equals("Email")) {
                emailService.sendWalletMail(applicationUser.getEmail());
            } else if (process.equals("SMS")) {
                smsService.send(new SmsPojo("+2" + applicationUser.getMobile(), "Welcome to movenopick entry system."));
            }
            return new ResponseEntity("", HttpStatus.OK);
        } catch (Exception e) {
            LoggerFactory.getLogger("new T");
            return new ResponseEntity("Can't send " + process, HttpStatus.BAD_REQUEST);
        }
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

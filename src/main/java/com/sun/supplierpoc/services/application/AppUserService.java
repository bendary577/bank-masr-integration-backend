package com.sun.supplierpoc.services.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.controllers.application.AppUserController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.*;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AppUserService {

    @Autowired
    private ApplicationUserRepo userRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private ImageService imageService;

    @Autowired
    QRCodeGenerator qrCodeGenerator;

    @Autowired
    SendEmailService emailService;

    @Autowired
    SmsService service;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ActionStatsService actionStatsService;

    @Autowired
    private AppUserController appUserController;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ApplicationUser saveUsers(ApplicationUser user){
        return userRepo.save(user);
    }

    public ArrayList<ApplicationUser> getActiveUsers(String accountId){
        return userRepo.findAllByAccountIdAndDeleted(accountId, false);
    }

    public ArrayList<ApplicationUser> getAppUsersByAccountId(String accountId){
        return userRepo.findAllByAccountIdOrderByCreationDateDesc(accountId);
    }

    public ApplicationUser getAppUserByCode(String guestCode, String accountCode){
        return userRepo.findByCodeAndAccountIdAndDeleted(guestCode, accountCode, false);
    }

    public List<ApplicationUser> getTopUsers(Account account) {

        List<ApplicationUser> applicationUsers = userRepo.findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(account.getId(), false, 0);

        return applicationUsers;
    }

    public HashMap addRewardPointsGuest(ApplicationUser applicationUser, MultipartFile image, Account account, GeneralSettings generalSettings) {
        HashMap response = new HashMap();

        if (applicationUser.getEmail() != null && applicationUser.getEmail().equals("")
                && userRepo.existsByEmailAndAccountId(applicationUser.getEmail(), account.getId())) {
            response.put("message", "There is user exist with this email.");
            response.put("success", false);
            return response;
        }

        String logoUrl = Constants.USER_IMAGE_URL;
        if (image != null) {
            try {
                logoUrl = imageService.store(image);
            } catch (Exception e) {
                response.put("message", "Can't save image due to error: " + e.getMessage() + ".");
                response.put("success", false);
                return response;

            }
        }

        if (applicationUser.getName() == null || applicationUser.getName().equals("")) {
            applicationUser.setName("- - - -");
        }
        applicationUser.setLogoUrl(logoUrl);
        applicationUser.setAccountId(account.getId());
        applicationUser.setCreationDate(new Date());
        applicationUser.setDeleted(false);

        /* Check mail settings validity */
        AccountEmailConfig emailConfig = account.getEmailConfig();
        if(emailConfig == null || (emailConfig.getHost().equals("")
                || emailConfig.getUsername().equals("")
                || emailConfig.getPassword().equals(""))){
            response.put("message", "Please check email settings and try again");
            response.put("success", false);
            return response;
        }

        String code = appUserController.createCode(applicationUser);
        String accountLogo = account.getImageUrl();
        String mailSubj = generalSettings.getMailSub();
        String QRPath = "QRCodes/" + code + ".png";
        applicationUser.setCode(code);

        try {
            String QrPath = qrCodeGenerator.getQRCodeImage(code, 200, 200, QRPath);
            if (emailService.sendMimeMail(QrPath, accountLogo, mailSubj, account.getName(), applicationUser, account)) {
                userRepo.save(applicationUser);
                response.put("message", "User added successfully.");
                response.put("success", true);
                return response;
            } else {
                response.put("message", "Invalid user email.");
                response.put("success", false);
                return response;
            }
        } catch (WriterException | IOException e) {
            response.put("message", e.getMessage());
            response.put("success", false);
            return response;
        }
    }

    public HashMap updateRewardPointsGuest(ApplicationUser applicationUser, MultipartFile image, Account account) {
        HashMap response = new HashMap();

        if (applicationUser.getEmail() != null && applicationUser.getEmail().equals("")
                && userRepo.existsByEmailAndAccountId(applicationUser.getEmail(), account.getId())) {
            response.put("message", "There is user exist with this email.");
            response.put("success", false);
            return response;
        }

        String logoUrl;
        if (image != null) {
            try {
                logoUrl = imageService.store(image);
                applicationUser.setLogoUrl(logoUrl);
            } catch (Exception e) {
                response.put("message", "Can't save image due to error: " + e.getMessage() + ".");
                response.put("success", false);
                return response;
            }
        }

        applicationUser.setLastUpdate(new Date());
        userRepo.save(applicationUser);

        response.put("message", "User updated successfully.");
        response.put("success", true);
        return response;
    }

    /////////////////////////////////////////////////////// *END* //////////////////////////////////////////////////////

    public HashMap addUpdateGuest(User agent, boolean addFlag, boolean isGeneric, String name, String email, String groupId, String userId,
                                  boolean sendEmail, boolean sendSMS, MultipartFile image, Account account, GeneralSettings generalSettings,
                                  String accompaniedGuestsJson, String balance, String cardCode, String expiryDate,
                                  String mobile) {

        HashMap response = new HashMap();

        Group group;
        ApplicationUser applicationUser = new ApplicationUser();

        if (addFlag) {

            Optional<Group> groupOptional = groupRepo.findById(groupId);
            if (groupOptional.isPresent()) {
                group = groupOptional.get();
                applicationUser.setGroup(group);
            } else {
                response.put("message", "User group can't be empty.");
                response.put("success", false);
                return response;
            }

            if (email != null && email.equals("") && userRepo.existsByEmailAndAccountId(email, account.getId())) {
                response.put("message", "There is user exist with this email.");
                response.put("success", false);
                return response;
            }

            String logoUrl = Constants.USER_IMAGE_URL;
            if (image != null) {
                try {
                    logoUrl = imageService.store(image);
                } catch (Exception e) {
                    response.put("message", "Can't save image due to error: " + e.getMessage() + ".");
                    response.put("success", false);
                    return response;

                }
            }

            if (name == null || name.equals("")) {
                name = "- - - -";
            }
            applicationUser.setName(name);
            applicationUser.setEmail(email);
            applicationUser.setLogoUrl(logoUrl);
            applicationUser.setMobile(mobile);
            applicationUser.setAccountId(account.getId());
            applicationUser.setCreationDate(new Date());
            applicationUser.setDeleted(false);
            if (!isGeneric) {
                /* Check mail settings validity */
                AccountEmailConfig emailConfig = account.getEmailConfig();
                if(emailConfig == null || (emailConfig.getHost().equals("")
                                || emailConfig.getUsername().equals("")
                                || emailConfig.getPassword().equals(""))){
                    response.put("message", "Please check email settings and try again");
                    response.put("success", false);
                    return response;
                }

                String code = appUserController.createCode(applicationUser);
                String accountLogo = account.getImageUrl();
                String mailSubj = generalSettings.getMailSub();
                String QRPath = "QRCodes/" + code + ".png";
                //encodeUserCOde(co);
                applicationUser.setCode(code);

                try {
                    String QrPath = qrCodeGenerator.getQRCodeImage(code, 200, 200, QRPath);
                    if (emailService.sendMimeMail(QrPath, accountLogo, mailSubj, account.getName(), applicationUser, account)) {
                        userRepo.save(applicationUser);
                        response.put("message", "User added successfully.");
                        response.put("success", true);
                        return response;
                    } else {
                        response.put("message", "Invalid user email.");
                        response.put("success", false);
                        return response;
                    }
                } catch (WriterException | IOException e) {
                    response.put("message", e.getMessage());
                    response.put("success", false);
                    return response;
                }
            }
            else {
                /* Check if card code is valid */
                ApplicationUser oldUser = userRepo.findByCodeAndAccountIdAndDeleted(cardCode, account.getId(), false);
                if(oldUser != null){
                    response.put("message", "Card code already used, Please enter a different card code.");
                    response.put("success", false);
                    return response;
                }

                ObjectMapper objectMapper = new ObjectMapper();
                List<AccompaniedGuests> accompaniedGuests = null;

                try {
                    accompaniedGuests = objectMapper.readValue(accompaniedGuestsJson, new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                applicationUser.setAccompaniedGuests(accompaniedGuests);

                applicationUser.setCode(cardCode);
                List<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();
                applicationUser.setWallet(new Wallet(List.of(new Balance(Double.parseDouble(balance), revenueCenters))));
                applicationUser.setGeneric(true);

                /* save expiry date 2022-01-06T14:28 */
                DateFormat fileDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm");
                Date expiry = null;
                try {
                    expiry = fileDateFormat.parse(expiryDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                applicationUser.setExpiryDate(expiry);

                if (sendEmail && email != null && !email.equals("")) {
                    emailService.sendWalletMail(email);
                } else if (sendEmail && (email == null || email.equals(""))) {
                    response.put("message", "Invalid Email");
                    response.put("success", false);
                    return response;
                }

                if (sendSMS && mobile != null && !mobile.equals("")) {
                    try {
                        service.send(new SmsPojo("+2" + mobile, "Welcome to movenpick entry system."));
                    } catch (Exception e) {
                        response.put("message", e.getMessage());
                        response.put("success", false);
                        return response;
                    }
                }
                else if (sendSMS && (mobile == null || mobile.equals(""))) {
                    response.put("message", "Invalid Mobile");
                    response.put("success", false);
                    return response;
                }

                WalletHistory walletHistory = new WalletHistory(ActionType.ENTRANCE_AMOUNT ,Double.parseDouble(balance) ,
                        0, Double.parseDouble(balance), agent, new Date());
                applicationUser.getWallet().getWalletHistory().add(walletHistory);

                userRepo.save(applicationUser);

                /* Create new user action */
                Action action = new Action();
                action.setUser(agent);
                action.setApplicationUser(applicationUser);
                action.setAccountId(agent.getAccountId());
                action.setAmount(Double.parseDouble(balance));
                action.setDate(new Date());
                action.setActionType(ActionType.ENTRANCE_AMOUNT);

                actionService.createUserAction(action);

                /* Update agent action stats */
                ActionStats actionStats = actionStatsService.findActionStatsByAgent(agent);
                if(actionStats == null){
                    actionStats = new ActionStats(agent, 0, 0,
                            Double.parseDouble(balance), agent.getAccountId());
                    actionStatsService.createActionStats(actionStats);
                }else {
                    actionStats.setEntranceAmount(actionStats.getEntranceAmount() + Double.parseDouble(balance));
                    actionStatsService.createActionStats(actionStats);
                }


                response.put("message", "User added successfully.");
                response.put("success", true);
                return response;
            }

        } else {
            Optional<ApplicationUser> userOptional = userRepo.findById(userId);

            if (userOptional.isPresent()) {

                applicationUser = userOptional.get();

                Optional<Group> groupOptional = groupRepo.findById(groupId);
                if (groupOptional.isPresent()) {
                    group = groupOptional.get();
                    applicationUser.setGroup(group);
                } else {
                    response.put("message", "User group can't be empty.");
                    response.put("success", false);
                    return response;
                }

                if (applicationUser.getEmail() != null && !applicationUser.getEmail().equals("") && !applicationUser.getEmail().equals(email)) {
                    if (userRepo.existsByEmailAndAccountId(email, account.getId())) {
                        response.put("message", "There is user exist with this email.");
                        response.put("success", false);
                        return response;
                    }
                }else if(email != null && email.equals("null")){
                    email = "";
                }

                String accountLogo;
                if(account.getImageUrl() != null && account.getImageUrl().equals("") ) {
                    accountLogo = account.getImageUrl();
                }else{
                    accountLogo = Constants.ACCOUNT_IMAGE_URL;
                }
                String mailSubj = generalSettings.getMailSub();
                String QRPath = "QRCodes/" + applicationUser.getCode() + ".png";
                applicationUser.setEmail(email);

                if (!isGeneric) {
                    try {
                        String QrPath = qrCodeGenerator.getQRCodeImage(applicationUser.getCode(), 200, 200, QRPath);
                        if (!emailService.sendMimeMail(QrPath, accountLogo, mailSubj, account.getName(), applicationUser, account)) {
                            response.put("message", "Invalid user email.");
                            response.put("success", false);
                            return response;
                        }
                    } catch (WriterException | IOException e) {
                        response.put("message", e.getMessage());
                        response.put("success", false);
                        return response;
                    }

                } else {
                    if (!accompaniedGuestsJson.equals("")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<AccompaniedGuests> accompaniedGuests = null;
                        try {
                            accompaniedGuests = objectMapper.readValue(accompaniedGuestsJson, new TypeReference<>() {
                            });
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        applicationUser.setAccompaniedGuests(accompaniedGuests);
                    }
                    if (sendEmail && email != null && !email.equals("")) {
                        emailService.sendWalletMail(email);
                    } else if (sendEmail && (email == null || email.equals(""))) {
                        response.put("message", "Invalid Email");
                        response.put("success", false);
                        return response;
                    }
                    if (sendSMS && mobile != null && !mobile.equals("")) {
                        LoggerFactory.getLogger("Bassel: ").info("Success");
                        try {
                            service.send(new SmsPojo("+2" + mobile, "Welcome to movenopick entry system."));
                        } catch (Exception e) {
                            response.put("message", e.getMessage());
                            response.put("success", false);
                            return response;
                        }
                    } else if (sendSMS && (mobile == null || mobile.equals(""))) {
                        response.put("message", "Invalid Mobile");
                        response.put("success", false);
                        return response;
                    }
                }
                if (image != null) {
                    String logoUrl;
                    try {
                        logoUrl = imageService.store(image);
                    } catch (Exception e) {
                        response.put("message", e.getMessage());
                        response.put("success", false);
                        return response;
                    }
                    applicationUser.setLogoUrl(logoUrl);
                }

                /* save expiry date 2022-01-06T14:28 */
                DateFormat fileDateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm");
                Date expiry = null;
                try {
                    expiry = fileDateFormat.parse(expiryDate);
                } catch (ParseException e) {
                    e.getMessage();
                }
                applicationUser.setExpiryDate(expiry);

                applicationUser.setName(name);
                applicationUser.setMobile(mobile);
                applicationUser.setAccountId(account.getId());
                group.setLastUpdate(new Date());

                userRepo.save(applicationUser);

                response.put("message", "User Updated successfully.");
                response.put("success", true);
                return response;
            } else {
                response.put("message", "Can't find user with this id.");
                response.put("success", false);
                return response;
            }
        }
    }

    public String encodeUserCOde(String code) {

        //                    String codeBuild = applicationUser.getName() + " " + group.getName() + " " + new Date().toString();
//                    Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
//                    byte[] encodedBytes = Base64.getEncoder().encode(codeBuild.getBytes());
//                    String code = new String(encodedBytes);
        return "";

    }

    public List<ApplicationUser> filterByParameters(String name, String fromDate, String toDate,
                                                    String cardNumber, String cardStatues,
                                                    String groupId) {


        return new ArrayList<>();

    }
}

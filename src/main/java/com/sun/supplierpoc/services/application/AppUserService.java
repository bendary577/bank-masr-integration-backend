package com.sun.supplierpoc.services.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.WriterException;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.applications.*;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.ImageService;
import com.sun.supplierpoc.services.QRCodeGenerator;
import com.sun.supplierpoc.services.SendEmailService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public List<ApplicationUser> getTopUsers(Account account) {

        List<ApplicationUser> applicationUsers = userRepo.findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(account.getId(), false, 0);

        return applicationUsers;
    }

    public HashMap addUpdateGuest(boolean addFlag, boolean isGeneric, String name, String email, String groupId,
                                  String userId, MultipartFile image, Account account, GeneralSettings generalSettings,
                                  String accompaniedGuestsJson, String balance, String cardCode, double expire, String mobile) {

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

            if (email!= null && email.equals("") && userRepo.existsByEmailAndAccountId(email, account.getId())) {
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

            if(name == null || name .equals("")){
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
                Random random = new Random();
                String code = applicationUser.getEmail().substring(0, applicationUser.getEmail().indexOf('@')) + random.nextInt(100);
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
            } else {

                ObjectMapper objectMapper = new ObjectMapper();
                List<AccompaniedGuests> accompaniedGuests = null;

                try {
                    accompaniedGuests = objectMapper.readValue(accompaniedGuestsJson, new TypeReference<>() {});
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                applicationUser.setAccompaniedGuests(accompaniedGuests);

                applicationUser.setCode(cardCode);
                List<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();
                applicationUser.setWallet(new Wallet(List.of(new Balance(Double.parseDouble(balance), revenueCenters))));
                applicationUser.setExpire(expire);

                userRepo.save(applicationUser);

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

                if (!applicationUser.getEmail().equals(email)) {

                    if (userRepo.existsByEmailAndAccountId(email, account.getId())) {
                        response.put("message", "There is user exist with this email.");
                        response.put("success", false);
                        return response;
                    }

                    String accountLogo = account.getImageUrl();
                    String mailSubj = generalSettings.getMailSub();
                    String QRPath = "QRCodes/" + applicationUser.getCode() + ".png";

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
                        applicationUser.setEmail(email);
                    }
                } else {
                    if (!accompaniedGuestsJson.equals("")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        List<AccompaniedGuests> accompaniedGuests = null;
                        try {
                            accompaniedGuests = objectMapper.readValue(accompaniedGuestsJson, new TypeReference<>() {});
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        applicationUser.setAccompaniedGuests(accompaniedGuests);
                    }
                    applicationUser.setEmail(email);
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

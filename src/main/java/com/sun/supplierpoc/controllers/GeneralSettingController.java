package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.WasteGroup;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.GeneralSettingsService;
import com.sun.supplierpoc.services.SendEmailService;
import com.sun.supplierpoc.services.TransferService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
public class GeneralSettingController {
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private GeneralSettingsService generalSettingsService;
    @Autowired
    private SendEmailService emailService;

    private final Conversions conversions = new Conversions();
    private final SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getOverGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getOverGroups(@RequestParam(name = "syncJobType") String syncTypeName, Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if(accountOptional.isPresent()){
            Account account = accountOptional.get();

            ArrayList<OverGroup> oldOverGroups = new ArrayList<>();
            if (syncTypeName != null && !syncTypeName.equals("")) {
                SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncTypeName, user.getAccountId(), false);
                oldOverGroups = syncJobType.getConfiguration().overGroups;
            } else {
                GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
                if (generalSettings != null) {
                    oldOverGroups = generalSettings.getOverGroups();
                } else {
                    generalSettings = new GeneralSettings(user.getAccountId(), new Date());
                    generalSettingsRepo.save(generalSettings);
                }
            }

            return generalSettingsService.getWastageGroups(account, oldOverGroups);
        }else{
            response.put("data", new ArrayList<>());
            response.put("message", "Account id provided does not exists!");
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/getGeneralSettings")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getGeneralSettings(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
            if (generalSettings != null) {
                return ResponseEntity.status(HttpStatus.OK).body(generalSettings);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get general settings.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to get general settings.");
        }
    }

    @RequestMapping("/updateGeneralSettings")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<?> updateGeneralSettings(Principal principal, @RequestBody GeneralSettings generalSettings) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {

            Account account = accountOptional.get();
            try {

                if (generalSettings.getSimphonyQuota() != null && generalSettings.getSimphonyQuota().getRevenueCenterQuota() != 0 &&
                        generalSettings.getRevenueCenters().size() > generalSettings.getSimphonyQuota().getRevenueCenterQuota()) {

                    response.put("message", "You have exceed you revenue center quota.");
                    response.put("success", false);
//                    emailService.sendAlertMail(account);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

                } else {
                    generalSettingsRepo.save(generalSettings);
                    response.put("message", "Update general settings successfully.");
                    response.put("success", true);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }

            } catch (Exception e) {
                response.put("message", "Failed to update general settings.");
                response.put("success", false);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            response.put("message", Constants.INVALID_USER);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }
}

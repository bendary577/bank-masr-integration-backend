package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
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

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getOverGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getOverGroups(@RequestParam(name = "syncJobType") String syncTypeName, Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
        ArrayList<OverGroup> oldOverGroups = new ArrayList<>();
        if (syncTypeName != null && !syncTypeName.equals("")){
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncTypeName, user.getAccountId(), false);
            oldOverGroups = syncJobType.getConfiguration().getOverGroups();
        }else{
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
            if (generalSettings != null){
                oldOverGroups = generalSettings.getOverGroups();
            }else {
                generalSettings = new GeneralSettings(user.getAccountId(), new ArrayList<>(),
                        new ArrayList<>(), new Date(), false);
                generalSettingsRepo.save(generalSettings);
            }
        }
        WebDriver driver;
        try{
            driver = setupEnvironment.setupSeleniumEnv(false);
        }
        catch (Exception ex){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }
        ArrayList<OverGroup> overGroups = new ArrayList<>();


        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("data", overGroups);
                return response;
            }
            String overGroupsURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/OverGroups/OverviewOverGroup.aspx";

            driver.get(overGroupsURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            for (int i = 14; i < rows.size(); i++) {
                OverGroup overGroup = new OverGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                // check existence of over group
                WebElement td = cols.get(columns.indexOf("over_group"));
                OverGroup oldOverGroupData = conversions.checkOverGroupExistence(oldOverGroups, td.getText().strip());

                if (oldOverGroupData.getChecked()) {
                    overGroup = oldOverGroupData;
                }
                else {
                    overGroup.setChecked(false);
                    overGroup.setOverGroup(td.getText().strip());
                }

                overGroups.add(overGroup);
            }

            driver.quit();

            response.put("cols", columns);
            response.put("data", overGroups);
            response.put("message", "Get over groups successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", overGroups);
            response.put("message", "Failed to get over groups.");
            response.put("success", false);

            return response;
        }
    }


    @RequestMapping("/updateOverGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> updateOverGroups(@RequestBody ArrayList<OverGroup> overGroups, Principal principal){
        HashMap<String, Object> response = new HashMap<>();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
            generalSettings.setOverGroups(overGroups);
            generalSettingsRepo.save(generalSettings);
            response.put("message", "Update over groups.");
            response.put("success", true);
        } catch (Exception e) {
            response.put("message", "Failed to update over groups.");
            response.put("success", false);
        }
        return response;
    }

    @RequestMapping("/getGeneralSettings")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getGeneralSettings(Principal principal){
        HashMap<String, Object> response = new HashMap<>();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
            response.put("data", generalSettings);
            response.put("message", "get general settings.");
            response.put("success", true);
        } catch (Exception e) {
            response.put("message", "Failed to get general settings.");
            response.put("success", false);
        }
        return response;
    }

    @RequestMapping("/updateGeneralSettings")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> updateGeneralSettings(@RequestBody GeneralSettings generalSettings){
        HashMap<String, Object> response = new HashMap<>();
        try {
            generalSettingsRepo.save(generalSettings);
            response.put("message", "Update over groups.");
            response.put("success", true);
        } catch (Exception e) {
            response.put("message", "Failed to update over groups.");
            response.put("success", false);
        }
        return response;
    }

}
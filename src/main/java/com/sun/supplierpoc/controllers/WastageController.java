package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import com.sun.supplierpoc.services.WastageService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

@RestController

public class WastageController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private TransferService transferService;
    @Autowired
    private WastageService wastageService;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getWastage")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getWastageRequest(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> response = getWastage(user.getId(), account);

        return response;
    }

    public HashMap<String, Object> getWastage(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.WASTAGE, account.getId());
        SyncJobType syncJobTypeJournal = syncJobTypeRepo.findByNameAndAccountId(Constants.JOURNALS, account.getId());

        if (!syncJobTypeJournal.getConfiguration().containsKey("costCenters") ||
                ((ArrayList<CostCenter>)syncJobTypeJournal.getConfiguration().get("costCenters")).size() == 0){
            String message = "Configure cost center before sync journals.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (!syncJobTypeJournal.getConfiguration().containsKey("items") ||
                ((ArrayList<HashMap<String, String>>)syncJobTypeJournal.getConfiguration().get("items")).size() == 0){
            String message = "Map items before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (!syncJobTypeJournal.getConfiguration().containsKey("'costCenterLocationMapping'") ||
                ((ArrayList<HashMap<String, String>>)syncJobTypeJournal.getConfiguration().get("'costCenterLocationMapping'")).size() == 0){
            String message = "Map cost centers to location before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (!((HashMap<String, String>)syncJobType.getConfiguration().get("accountSettings")).containsKey("wastageGroupIdStarting")){
            String message = "Configure group id starting before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        try {

            HashMap<String, Object> data = wastageService.getWastageData(syncJobTypeJournal, syncJobTypeJournal, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> wastes = (ArrayList<HashMap<String, String>>) data.get("wastes");
                if (wastes.size() > 0) {
                    ArrayList<SyncJobData> addedWastes = wastageService.saveWastageSunData(wastes, syncJob);
                    InvoiceController.handleSendTransfer(syncJobType, syncJobTypeJournal, syncJob, addedWastes, transferService, syncJobDataRepo);
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no wastage to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no wastage to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get wastage from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }

            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/getWasteGroups")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getWasteGroups(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.WASTAGE, user.getAccountId());
        ArrayList<HashMap<String, String>> oldWasteTypes = (ArrayList<HashMap<String, String>>) syncJobType.getConfiguration().get("overGroups");

        if (!syncJobType.getConfiguration().containsKey("wasteGroups")){
            String message = "Error in getting old waste groups, please contact support team.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        ArrayList<HashMap<String, Object>> wasteTypes = new ArrayList<>();

        try {
            String url = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/FormLogin.aspx";

            if (!setupEnvironment.loginOHIM(driver, url, account)) {
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("data", wasteTypes);
                return response;
            }

            String wasteTypesURL = "https://mte03-ohim-prod.hospitality.oracleindustry.com/Webclient/MasterData/WasteGroups/WasteGroup.aspx";
            driver.get(wasteTypesURL);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            for (int i = 14; i < rows.size(); i++) {
                HashMap<String, Object> wasteType = new HashMap<>();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                for (int j = 0; j < cols.size(); j++) {
                    wasteType.put(columns.get(j), cols.get(j).getText());
                }

                // check existence of over group
                WebElement td = cols.get(columns.indexOf("waste_group"));
                HashMap<String, Object> oldWasteTypesData = conversions.checkWasteTypeExistence(oldWasteTypes, td.getText().strip());
                HashMap<String, String> oldWasteType = (HashMap<String, String>)oldWasteTypesData.get("wasteType");

                if ((boolean) oldWasteTypesData.get("status"))
                    wasteType.put("checked", true);
                else
                    wasteType.put("checked", false);

                wasteTypes.add(wasteType);
            }

            driver.quit();

            response.put("cols", columns);
            response.put("data", wasteTypes);
            response.put("message", "Get wastes successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", wasteTypes);
            response.put("message", "Failed to get wastes.");
            response.put("success", false);

            return response;
        }
    }

}

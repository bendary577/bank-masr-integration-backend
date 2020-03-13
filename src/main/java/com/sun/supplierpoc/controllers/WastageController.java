package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.WasteGroup;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import com.sun.supplierpoc.services.WastageService;
import com.systemsunion.security.IAuthenticationVoucher;
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
    @Autowired
    private InvoiceController invoiceController;

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
        SyncJobType syncJobTypeJournal = syncJobTypeRepo.findByNameAndAccountId(Constants.CONSUMPTION, account.getId());

        if (syncJobTypeJournal.getConfiguration().getCostCenters().size() == 0){
            String message = "Configure cost center before sync journals.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobTypeJournal.getConfiguration().getItems().size() == 0){
            String message = "Map items before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobTypeJournal.getConfiguration().getCostCenterLocationMapping().size() == 0){
            String message = "Map cost centers to location before sync wastage.";
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
                    IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                    if (voucher != null){
                        invoiceController.handleSendJournal(syncJobType, syncJobTypeJournal, syncJob, addedWastes, account, voucher);
                        syncJob.setReason("");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);

                        response.put("message", "Sync Invoices Successfully.");
                    }
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
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.WASTAGE, user.getAccountId());
        ArrayList<WasteGroup> oldWasteTypes = syncJobType.getConfiguration().getWasteGroups();

        WebDriver driver = setupEnvironment.setupSeleniumEnv(false);
        HashMap<String, Object> response = new HashMap<>();

        if (driver == null){
            response.put("status", Constants.FAILED);
            response.put("message", "Failed to establish connection with firefox driver.");
            response.put("invoices", new ArrayList<>());
            return response;
        }
        ArrayList<WasteGroup> wasteTypes = new ArrayList<>();

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
                WasteGroup wasteType = new WasteGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                // check existence of over group
                WebElement td = cols.get(columns.indexOf("waste_group"));
                WasteGroup oldWasteTypesData = conversions.checkWasteTypeExistence(oldWasteTypes, td.getText().strip());

                if (oldWasteTypesData.getChecked()){
                    wasteType= oldWasteTypesData;
                }

                else{
                    wasteType.setChecked(false);
                    wasteType.setWasteGroup(td.getText().strip());
                }

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

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.TransfersExcelExporter;
import com.sun.supplierpoc.excelExporters.WastageExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.*;
import com.systemsunion.security.IAuthenticationVoucher;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
// @RequestMapping(path = "server")


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
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private WastageService wastageService;
    @Autowired
    private SunService sunService;
    @Autowired
    private InvoiceController invoiceController;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;


    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getWastage")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getWastageRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getWastage(user.getId(), account);
            if(response.get("success").equals(false)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
        String message = "Invalid Credentials";
        response.put("message", message);
        response.put("success", false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    public HashMap<String, Object> getWastage(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType wastageSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.WASTAGE, account.getId(), false);

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<WasteGroup> wasteGroups = wastageSyncJobType.getConfiguration().getWasteGroups();

        String timePeriod = wastageSyncJobType.getConfiguration().getTimePeriod();
        String fromDate = wastageSyncJobType.getConfiguration().getFromDate();
        String toDate = wastageSyncJobType.getConfiguration().getToDate();

        ArrayList<OverGroup> overGroups;
        if (!wastageSyncJobType.getConfiguration().getUniqueOverGroupMapping()){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  wastageSyncJobType.getConfiguration().getOverGroups();
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(wastageSyncJobType);
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (timePeriod.equals("")){
            String message = "Configure business date before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Configure business date before sync wastage.";

                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Configure cost center before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItems().size() == 0){
            String message = "Map items before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getCostCenterLocationMapping().size() == 0){
            String message = "Map cost centers to location before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), wastageSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        ArrayList<SyncJobData> addedWastes = new ArrayList<>();
        try {

            HashMap<String, Object> data = wastageService.getWastageData(wastageSyncJobType, items, costCenters,
                    overGroups, wasteGroups, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> wastes = (ArrayList<HashMap<String, String>>) data.get("wastes");
                if (wastes.size() > 0) {
                    addedWastes = wastageService.saveWastageSunData(wastes, syncJob);
                    if(addedWastes.size() > 0  && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(wastageSyncJobType, syncJob, addedWastes, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedWastes.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync Wastage Successfully.");
                            response.put("success", true);
                        }
                    }
                    else if (addedWastes.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
                        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

                        String username = sunCredentials.getUsername();
                        String password = sunCredentials.getPassword();
                        String host = sunCredentials.getHost();

                        FtpClient ftpClient = new FtpClient(host, username, password);

                        if(ftpClient.open()){
                            List<SyncJobData> creditNotesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
                            SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter(
                                    wastageSyncJobType, creditNotesList);

                            DateFormatSymbols dfs = new DateFormatSymbols();
                            String[] weekdays = dfs.getWeekdays();

                            String transactionDate = creditNotesList.get(0).getData().get("transactionDate");
                            Calendar cal = Calendar.getInstance();
                            Date date = new SimpleDateFormat("ddMMyyyy").parse(transactionDate);
                            cal.setTime(date);
                            int day = cal.get(Calendar.DAY_OF_WEEK);

                            String dayName = weekdays[day];
                            String fileExtension = ".ndf";
                            String fileName = dayName.substring(0,3) + transactionDate + fileExtension;
                            File file = excelExporter.createNDFFile();

                            boolean sendFileFlag = false;
                            try {
                                sendFileFlag = ftpClient.putFileToPath(file, fileName);
                                ftpClient.close();
                            } catch (IOException e) {
                                ftpClient.close();
                            }

                            if (sendFileFlag){
//                            if (true){
                                syncJobDataService.updateSyncJobDataStatus(creditNotesList, Constants.SUCCESS);
                                syncJobService.saveSyncJobStatus(syncJob, addedWastes.size(),
                                        "Sync wastage successfully.", Constants.SUCCESS);

                                response.put("success", true);
                                response.put("message", "Sync wastage successfully.");
                            }else {
                                syncJobDataService.updateSyncJobDataStatus(creditNotesList, Constants.FAILED);
                                syncJobService.saveSyncJobStatus(syncJob, addedWastes.size(),
                                        "Failed to sync wastage to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to sync wastage to sun system via FTP.");
                            }
                        }
                        else {
                            syncJobService.saveSyncJobStatus(syncJob, addedWastes.size(),
                                    "Failed to connect to sun system via FTP.", Constants.FAILED);

                            response.put("success", false);
                            response.put("message", "Failed to connect to sun system via FTP.");
                        }
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no wastage to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedWastes.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no wastage to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get wastage from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedWastes.size());
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }

            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedWastes.size());
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

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.WASTAGE, user.getAccountId(), false);
        ArrayList<WasteGroup> oldWasteTypes = syncJobType.getConfiguration().getWasteGroups();
        HashMap<String, Object> response = new HashMap<>();

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
        ArrayList<WasteGroup> wasteTypes = new ArrayList<>();

        try {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)) {
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

    @GetMapping("/wastage/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Wastage" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> wastageList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,false);

        WastageExcelExporter excelExporter = new WastageExcelExporter(wastageList);

        excelExporter.export(response);
    }
}

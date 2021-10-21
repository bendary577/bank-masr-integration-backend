package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
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
    @Autowired
    private RoleService roleService;

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
            response = getWastage(user.getId(), account, user);
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

    public HashMap<String, Object> getWastage(String userId, Account account, User user) {
        if (user != null && roleService.hasRole(user, Constants.GENERATE_WASTAGE_REPORT)){
            System.out.println("This account has generate wastage report");
        }
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType wastageSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.WASTAGE, account.getId(), false);

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<WasteGroup> wasteGroups = wastageSyncJobType.getConfiguration().wastageConfiguration.wasteGroups;

        String timePeriod = wastageSyncJobType.getConfiguration().timePeriod;
        String fromDate = wastageSyncJobType.getConfiguration().fromDate;
        String toDate = wastageSyncJobType.getConfiguration().toDate;

        ArrayList<OverGroup> overGroups;
        if (!wastageSyncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  wastageSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(wastageSyncJobType, account.getERD());
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

        if (generalSettings.getLocations().size() == 0){
            String message = "Map cost centers to location before sync wastage.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), wastageSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        ArrayList<JournalBatch> wasteBatches = new ArrayList<>();
        try {
            Response data;
            if(wastageSyncJobType.getConfiguration().wastageConfiguration.wasteReport.equals(Constants.INVENTORY_WASTE)){
                data = wastageService.getWastageData(wastageSyncJobType, items, itemGroups, costCenters,
                        overGroups, wasteGroups, account);
                wasteBatches.add(new JournalBatch(new CostCenter(), data.getWaste()));
            }
            else{
                data = wastageService.getWastageReportData(wastageSyncJobType, generalSettings, account);
                wasteBatches = data.getJournalBatches();
            }

            if (data.isStatus()) {
                if (wasteBatches.size() > 0) {
                    wastageService.saveWastageSunData(wasteBatches, syncJob);

                    /* Check generate waste report feature */
                    if (wasteBatches.size() > 0 && user != null && roleService.hasRole(user, Constants.GENERATE_WASTAGE_REPORT)){
                        System.out.println("This account has generate wastage report");
                        WastageExcelExporter excelExporter = new WastageExcelExporter();
                        excelExporter.exportMonthlyReport(account.getName(),generalSettings, wastageSyncJobType, wasteBatches);

                        syncJob.setReason("");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(wasteBatches.size());
                        syncJobRepo.save(syncJob);

                        response.put("message", "Sync and generate Wastage Successfully.");
                        response.put("success", true);
                    }
                    else if(wasteBatches.size() > 0  && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            for (JournalBatch batch : wasteBatches) {
                                invoiceController.handleSendJournal(wastageSyncJobType, syncJob, batch.getWasteData(), account, voucher);
                            }

                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(wasteBatches.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync Wastage Successfully.");
                            response.put("success", true);
                        }else{
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(wasteBatches.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Failed to open connection with SUN.");
                            response.put("success", true);
                        }
                    }

                    else if (wasteBatches.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        List<SyncJobData> wasteList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);

                        if(wastageSyncJobType.getConfiguration().exportFilePerLocation){
                            ArrayList<File> files = createWastesFilePerLocation(wasteBatches, wastageSyncJobType, account.getName());
                        }else {
                            SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(wastageSyncJobType, wasteList);
                            File file = exporter.prepareNDFFile(wasteList, wastageSyncJobType, account.getName(), "");
                        }

                        if(ftpClient != null){
                            if(ftpClient.open()){
//                                boolean sendFileFlag = false;
//                                try {
//                                    sendFileFlag = ftpClient.putFileToPath(file, file.getName());
//                                    ftpClient.close();
//                                } catch (IOException e) {
//                                    ftpClient.close();
//                                }
//                                if (sendFileFlag){
                            if (true){
                                    syncJobDataService.updateSyncJobDataStatus(wasteList, Constants.SUCCESS);
                                    syncJobService.saveSyncJobStatus(syncJob, wasteBatches.size(),
                                            "Sync wastage successfully.", Constants.SUCCESS);

                                    response.put("success", true);
                                    response.put("message", "Sync wastage successfully.");
                                }else {
                                    syncJobDataService.updateSyncJobDataStatus(wasteList, Constants.FAILED);
                                    syncJobService.saveSyncJobStatus(syncJob, wasteBatches.size(),
                                            "Failed to sync wastage to sun system via FTP.", Constants.FAILED);

                                    response.put("success", false);
                                    response.put("message", "Failed to sync wastage to sun system via FTP.");
                                }
                            }
                            else {
                                syncJobService.saveSyncJobStatus(syncJob, wasteBatches.size(),
                                        "Failed to connect to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to connect to sun system via FTP.");
                            }
                        }else{
                            syncJobDataService.updateSyncJobDataStatus(wasteList, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, wasteBatches.size(),
                                    "Sync wastage successfully.", Constants.SUCCESS);

                            response.put("success", true);
                            response.put("message", "Sync wastage successfully.");
                        }
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no wastage to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(wasteBatches.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no wastage to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get wastage from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(wasteBatches.size());
                syncJobRepo.save(syncJob);

                response.put("message", data.getMessage());
                response.put("success", false);
            }

            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(wasteBatches.size());
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
        ArrayList<WasteGroup> oldWasteTypes = syncJobType.getConfiguration().wastageConfiguration.wasteGroups;
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

            driver.get(Constants.WASTE_GROUPS_LINK);

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

            syncJobType.getConfiguration().wastageConfiguration.wasteGroups = wasteTypes;
            syncJobTypeRepo.save(syncJobType);
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

    @GetMapping("/generateWastageMonthlyReport")
    public void generateWastageMonthlyReport(Principal principal, HttpServletResponse response) throws IOException {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.WASTAGE, user.getAccountId(), false);
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=Wastage" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);

        }
    }

    private ArrayList<File> createWastesFilePerLocation(List<JournalBatch> wasteBatches, SyncJobType syncJobType,
                                                       String AccountName) {
        ArrayList<File> locationFiles = new ArrayList<>();

        try {
            File file;
            List<SyncJobData> wasteList;
            SalesFileDelimiterExporter excelExporter;

            for (JournalBatch locationBatch : wasteBatches) {
                wasteList = new ArrayList<>(locationBatch.getWasteData());
 
                excelExporter = new SalesFileDelimiterExporter(syncJobType, wasteList);
                file = excelExporter.prepareNDFFile(wasteList, syncJobType, AccountName, locationBatch.getCostCenter().costCenterReference);
                if(file != null)
                    locationFiles.add(file);
            }
            return locationFiles;
        }catch (Exception e){
            e.printStackTrace();
            return locationFiles;
        }
    }
}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.TransfersExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.SunService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import com.sun.supplierpoc.services.TransferService;
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


public class TransferController {
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
    private TransferService transferService;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;
    @Autowired
    private InvoiceController invoiceController;

    private Conversions conversions = new Conversions();
    private SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getBookedTransferRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getBookedTransfer(user.getId(), account);
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

    public HashMap<String, Object> getBookedTransfer(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType transferSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.TRANSFERS, account.getId(), false);

        ArrayList<CostCenter> costCenters =  generalSettings.getCostCenterAccountMapping();
        ArrayList<Item> items =  generalSettings.getItems();
        ArrayList<OverGroup> overGroups;

        String timePeriod = transferSyncJobType.getConfiguration().timePeriod;
        String fromDate = transferSyncJobType.getConfiguration().fromDate;
        String toDate = transferSyncJobType.getConfiguration().toDate;


        if (!transferSyncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  transferSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(transferSyncJobType, account.getERD());
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (timePeriod.equals("")){
            String message = "Configure business date before sync transfers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Configure business date before sync transfers.";

                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync transfers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItems().size() == 0){
            String message = "Map items before sync transfers.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId, account.getId(),
                transferSyncJobType.getId(), 0);
        syncJobRepo.save(syncJob);

        ArrayList<SyncJobData> addedTransfers = new ArrayList<>();

        try {

            HashMap<String, Object> data = transferService.getTransferData(transferSyncJobType, costCenters, items,
                    overGroups, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> transfers = (ArrayList<HashMap<String, String>>) data.get("transfers");
                if (transfers.size() > 0) {
                    addedTransfers = transferService.saveTransferSunData(transfers, syncJob);
                    if(addedTransfers.size() > 0  && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(transferSyncJobType, syncJob, addedTransfers, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedTransfers.size());
                            syncJobRepo.save(syncJob);

                            response.put("success", true);
                            response.put("message", "Sync transfers Successfully.");
                        }
                    } 
                    else if (addedTransfers.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);
                        SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(transferSyncJobType, addedTransfers);

                        if(ftpClient != null && ftpClient.open()){
                            File file = exporter.prepareNDFFile(addedTransfers, transferSyncJobType, account.getName());

                            boolean sendFileFlag = false;
                            try {
                                sendFileFlag = ftpClient.putFileToPath(file, file.getName());
                                ftpClient.close();
                            } catch (IOException e) {
                                ftpClient.close();
                            }

                            if (sendFileFlag){
//                            if (true){
                                syncJobDataService.updateSyncJobDataStatus(addedTransfers, Constants.SUCCESS);
                                syncJobService.saveSyncJobStatus(syncJob, addedTransfers.size(),
                                        "Sync transfers successfully.", Constants.SUCCESS);

                                response.put("success", true);
                                response.put("message", "Sync transfers successfully.");
                            }else {
                                syncJobDataService.updateSyncJobDataStatus(addedTransfers, Constants.FAILED);
                                syncJobService.saveSyncJobStatus(syncJob, addedTransfers.size(),
                                        "Failed to sync transfers to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to sync transfers to sun system via FTP.");
                            }
                        }
                        else {
                            syncJobService.saveSyncJobStatus(syncJob, addedTransfers.size(),
                                    "Failed to connect to sun system via FTP.", Constants.FAILED);

                            response.put("success", false);
                            response.put("message", "Failed to connect to sun system via FTP.");
                        }
                    }
                } else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no transfers to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedTransfers.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no transfers to get from Oracle Hospitality.");
                    response.put("success", true);
                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get transfers from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedTransfers.size());
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }
            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedTransfers.size());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    @RequestMapping("/mapItems")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> mapItems(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);

        ArrayList<OverGroup> oldOverGroups = generalSettings.getOverGroups();
        ArrayList<MajorGroup> majorGroups = new ArrayList<>();
        ArrayList<ItemGroup> itemGroups = new ArrayList<>();
        ArrayList<Item> items = new ArrayList<>();
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

        try {
            if (checkLogin(items, driver, response, Constants.OHIM_LOGIN_LINK, account)) return response;

            driver.get(Constants.MAJOR_GROUPS_LINK);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            // Getting MajorGroups
            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);
            for (int i = 14; i < rows.size(); i++) {
                MajorGroup majorGroup = new MajorGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }

                majorGroup.setOverGroup(cols.get(columns.indexOf("over_group")).getText().strip());
                majorGroup.setMajorGroup(cols.get(columns.indexOf("major_group")).getText().strip());

                // check if this major group belong to chosen over group
                OverGroup overGroupData = conversions.checkOverGroupExistence(oldOverGroups, majorGroup.getOverGroup());
                if (overGroupData.getChecked()) {
                    majorGroup.setChecked(true);
                    majorGroups.add(majorGroup);
                }
            }

            if (majorGroups.size() == 0) {
                driver.quit();

                response.put("data", items);
                response.put("message", "There is no major groups in over group selected.");
                response.put("success", true);

                return response;
            }
            // save new major groups
            generalSettings.setMajorGroups(majorGroups);

            // Get Items Group
            driver.findElement(By.partialLinkText("Main Menu")).click();
            driver.findElement(By.name("_ctl31")).click();
            driver.findElement(By.id("link-27-10-4-ItemGroups")).click();

//            driver.get(Constants.MAIN_MENU_URL);
//            driver.get(Constants.ITEMS_GROUPS_LINK);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            rows = driver.findElements(By.tagName("tr"));

            columns = setupEnvironment.getTableColumns(rows, true, 10);
            for (int i = 11; i < rows.size(); i++) {
                ItemGroup itemGroup = new ItemGroup();

                WebElement row = rows.get(i);
                List<WebElement> cols = row.findElements(By.tagName("td"));

                if (cols.size() != columns.size()) {
                    continue;
                }
                itemGroup.setItemGroup(cols.get(columns.indexOf("item_group")).getText().strip());
                itemGroup.setMajorGroup(cols.get(columns.indexOf("major_group")).getText().strip());

                // check if this Item group belong to chosen major group
                MajorGroup majorGroupData = conversions.checkMajorGroupExistence(majorGroups, itemGroup.getMajorGroup());

                if (majorGroupData.getChecked()) {
                    itemGroup.setChecked(true);
                    itemGroup.setOverGroup(majorGroupData.getOverGroup());
                    itemGroups.add(itemGroup);
                }
            }

            if (itemGroups.size() == 0) {
                driver.quit();

                response.put("data", items);
                response.put("message", "There is no item groups in major group selected.");
                response.put("success", true);

                return response;
            }
            // save new Item groups
            generalSettings.setItemGroups(itemGroups);

            // Get Items Group
            driver.findElement(By.partialLinkText("Main Menu")).click();
            driver.findElement(By.name("_ctl31")).click();
            driver.findElement(By.id("link-27-10-2-Items")).click();
//            driver.get(Constants.MAIN_MENU_URL);
//            driver.get(Constants.ITEMS_LINK);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();
            rows = driver.findElements(By.tagName("tr"));
            columns = setupEnvironment.getTableColumns(rows, true, 17);

            while (true){
                for (int i = 11; i < rows.size(); i++) {
                    Item item = new Item();

                    WebElement row = rows.get(i);
                    List<WebElement> cols = row.findElements(By.tagName("td"));

                    if (cols.size() != columns.size()) {
                        continue;
                    }

                    item.setItem(cols.get(columns.indexOf("item")).getText().strip());
                    String itemGroupText = cols.get(columns.indexOf("item_group")).getText().strip();

                    ItemGroup itemGroup = conversions.checkItemGroupExistence(itemGroups, itemGroupText);
                    if (itemGroup.getChecked()){
                        item.setItemGroup(itemGroupText);
                        item.setMajorGroup(itemGroup.getMajorGroup());
                        item.setOverGroup(itemGroup.getOverGroup());
                        item.setChecked(true);

                        items.add(item);
                    }
                }
                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    boolean paginationFlag = TransferService.checkPagination(driver, "dg_rc_0_2");
                    if (!paginationFlag){
                        driver.quit();

                        response.put("data", items);
                        response.put("message", "Failed to map over groups to items.");
                        response.put("success", false);

                        return response;
                    }
                    rows = driver.findElements(By.tagName("tr"));
                }
            }

            // save new items
            generalSettings.setItems(items);

            generalSettingsRepo.save(generalSettings);

            driver.quit();

            response.put("cols", columns);
            response.put("data", items);
            response.put("message", "Get map over groups to items successfully.");
            response.put("success", true);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            driver.quit();

            response.put("data", items);
            response.put("message", "Failed to map over groups to items.");
            response.put("success", false);

            return response;
        }
    }

    private boolean checkLogin(ArrayList<Item> items, WebDriver driver, HashMap<String, Object> response,
                               String url, Account account) {
        if (!setupEnvironment.loginOHIM(driver, url, account)) {
            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", "Invalid username and password.");
            response.put("data", items);
            return true;
        }
        return false;
    }

    @GetMapping("/transfers/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BookedTransfers_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> bookedTransfersList = syncJobDataRepo.findBySyncJobIdAndDeleted("5f8c2525bba1d80a32d30c1b",
                false);

        TransfersExcelExporter excelExporter = new TransfersExcelExporter(bookedTransfersList);

        excelExporter.export(response);
    }

    @GetMapping("/transfers/export/csv")
    public void exportToText(Principal principal,
                             @RequestParam(name = "syncJobId") String syncJobId,
                             HttpServletResponse response) throws IOException {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Transfers" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        response.setContentType("text/csv");

        List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,
                false);

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.TRANSFERS, account.getId(), false);

        SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter("Transfers.ndf", syncJobType, salesList);

//        excelExporter.writeSyncData(response.getWriter());
    }

}

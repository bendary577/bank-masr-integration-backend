package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.InvoicesExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.*;
import com.sun.supplierpoc.soapModels.Supplier;
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
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
// @RequestMapping(path = "server")

public class InvoiceController {
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
    private InvoiceService invoiceService;
    @Autowired
    private SunService sunService;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;

    public Conversions conversions = new Conversions();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    public InvoiceController() {}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getApprovedInvoices")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getApprovedInvoicesRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getApprovedInvoices(user.getId(), account);
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

    public HashMap<String, Object> getApprovedInvoices(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType invoiceSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.APPROVED_INVOICES, account.getId(), false);

        String invoiceTypeIncluded = invoiceSyncJobType.getConfiguration().invoiceConfiguration.invoiceTypeIncluded;
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<Supplier> suppliers = generalSettings.getSuppliers();

        ArrayList<Item> items =  generalSettings.getItems();

        String timePeriod = invoiceSyncJobType.getConfiguration().timePeriod;
        String fromDate = invoiceSyncJobType.getConfiguration().fromDate;
        String toDate = invoiceSyncJobType.getConfiguration().toDate;

        ArrayList<OverGroup> overGroups ;
        if (!invoiceSyncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  invoiceSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(invoiceSyncJobType, account.getERD());
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (invoiceTypeIncluded.equals("")){
            String message = "Configure invoice types before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (timePeriod.equals("")){
            String message = "Map time period before sync credit notes.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Map time period before sync credit notes.";
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItems().size() == 0){
            String message = "Map items before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), invoiceSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        try {
            HashMap<String, Object> data ;
            ArrayList<HashMap<String, Object>> invoices ;

            int invoiceType = 3; // BOTH

            if (invoiceTypeIncluded.equals(Constants.APPROVED_INVOICE)){
                invoiceType = 1;
            }
            else if (invoiceTypeIncluded.equals(Constants.ACCOUNT_PAYABLE)){
                invoiceType = 2;
            }

            data = invoiceService.getInvoicesReceiptsData(false,invoiceType, invoiceSyncJobType.getConfiguration(),
                    costCenters, suppliers, items, overGroups, account, timePeriod, fromDate, toDate);

            invoices = (ArrayList<HashMap<String, Object>>) data.get("invoices");

            if (data.get("status").equals(Constants.SUCCESS)){
                if (invoices.size() > 0){
                    addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, invoiceSyncJobType, false);
                    if (addedInvoices.size() > 0 && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            handleSendJournal(invoiceSyncJobType, syncJob, addedInvoices, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedInvoices.size());
                            syncJobRepo.save(syncJob);

                            response.put("success", true);
                            response.put("message", "Sync Invoices Successfully.");
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedInvoices.size());
                            syncJobRepo.save(syncJob);

                            response.put("success", false);
                            response.put("message", "Failed to connect to Sun System.");
                            return response;
                        }
                    }
                    else if (addedInvoices.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);

                        SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(invoiceSyncJobType, addedInvoices);
                        File file = exporter.prepareNDFFile(addedInvoices, invoiceSyncJobType, account.getName(), "");

                        if(ftpClient != null){
                            if(ftpClient.open()){

                                boolean sendFileFlag = false;
                                try {
                                    sendFileFlag = ftpClient.putFileToPath(file, file.getName());
                                    ftpClient.close();
                                } catch (IOException e) {
                                    ftpClient.close();
                                }

                                if (sendFileFlag){
                                    syncJobDataService.updateSyncJobDataStatus(addedInvoices, Constants.SUCCESS);
                                    syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                            "Sync approved invoices successfully.", Constants.SUCCESS);

                                    response.put("success", true);
                                    response.put("message", "Sync approved invoices successfully.");
                                }
                                else {
                                    syncJobDataService.updateSyncJobDataStatus(addedInvoices, Constants.FAILED);
                                    syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                            "Failed to sync approved invoices to sun system via FTP.", Constants.FAILED);

                                    response.put("success", false);
                                    response.put("message", "Failed to sync approved invoices to sun system via FTP.");
                                }
                            }
                            else {
                                syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                        "Failed to connect to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to connect to sun system via FTP.");
                            }
                        }else {
                            syncJobDataService.updateSyncJobDataStatus(addedInvoices, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                    "Sync approved Invoices successfully.", Constants.SUCCESS);

                            response.put("success", true);
                            response.put("message", "Sync sales successfully.");
                        }

                    }
                    else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No new invoices to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(addedInvoices.size());
                        syncJobRepo.save(syncJob);

                        response.put("success", true);
                        response.put("message", "No new invoices to add in middleware.");
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no invoices to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedInvoices.size());
                    syncJobRepo.save(syncJob);

                    response.put("success", true);
                    response.put("message", "There is no invoices to get from Oracle Hospitality.");
                }
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason((String) data.get("message"));
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedInvoices.size());
                syncJobRepo.save(syncJob);

                response.put("success", false);
                response.put("message", "Failed to get invoices from Oracle Hospitality.");
            }
        }
        catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedInvoices.size());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
        }
        return response;
    }

    void handleSendJournal(SyncJobType syncJobType, SyncJob syncJob,
                           ArrayList<SyncJobData> addedJournals, Account account, IAuthenticationVoucher voucher) {
        HashMap<String, Object> data;
        for (SyncJobData addedJournal : addedJournals) {
            try {
                data  = sunService.sendJournalData(addedJournal, null, syncJobType, account, voucher);
                if ((Boolean) data.get("status")){
                    addedJournal.setStatus(Constants.SUCCESS);
                    addedJournal.setReason("");
                }
                else {
                    addedJournal.setStatus(Constants.FAILED);
                    addedJournal.setReason((String) data.get("message"));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());

                addedJournal.setStatus(Constants.FAILED);
                addedJournal.setReason(e.getMessage());
            }
            syncJobDataRepo.save(addedJournal);
        }
        syncJob.setStatus(Constants.SUCCESS);
    }

    @RequestMapping("/getCostCenter")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCostCenter(@RequestParam(name = "syncTypeName") String syncTypeName,
                                                 Principal principal){
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

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
        ArrayList<CostCenter> costCenters = new ArrayList<>();

        ArrayList<CostCenter> oldCostCenters = new ArrayList<>();
        if (syncTypeName != null && !syncTypeName.equals("")){
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncTypeName, user.getAccountId(),
                    false);

            oldCostCenters = syncJobType.getConfiguration().costCenters;

        }else{
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(user.getAccountId(), false);
            if (generalSettings != null){
                oldCostCenters = generalSettings.getCostCenterAccountMapping();
            }else {
                generalSettings = new GeneralSettings(user.getAccountId(), new Date());
                generalSettingsRepo.save(generalSettings);
            }
        }

        try
        {
            if (!setupEnvironment.loginOHIM(driver, Constants.OHIM_LOGIN_LINK, account)){
                driver.quit();

                response.put("status", Constants.FAILED);
                response.put("message", "Invalid username and password.");
                response.put("costCenters", costCenters);
                return response;
            }

            driver.get(Constants.COST_CENTERS_LINK);

            driver.findElement(By.name("filterPanel_btnRefresh")).click();

            List<WebElement> rows = driver.findElements(By.tagName("tr"));

            ArrayList<String> columns = setupEnvironment.getTableColumns(rows, true, 13);

            while (true){
                fillCostCenterObject(costCenters, rows, oldCostCenters, columns);

                // check if there is other pages
                if (driver.findElements(By.linkText("Next")).size() == 0){
                    break;
                }
                else {
                    TransferService.checkPagination(driver, "dg_rc_0_1");
                    rows = driver.findElements(By.tagName("tr"));
                }
            }
            driver.quit();

            response.put("status", Constants.SUCCESS);
            response.put("message", "Get cost centers successfully");
            response.put("costCenters", costCenters);
            return response;

        }catch (Exception e) {
            e.printStackTrace();

            driver.quit();

            response.put("status", Constants.FAILED);
            response.put("message", e);
            response.put("costCenters", costCenters);
            return response;
        }
    }

    private void fillCostCenterObject(ArrayList<CostCenter> costCenters, List<WebElement> rows,
                                      ArrayList<CostCenter> oldCostCenters, ArrayList<String> columns){

        for (int i = 14; i < rows.size(); i++) {
            CostCenter costCenter = new CostCenter();

            WebElement row = rows.get(i);
            List<WebElement> cols = row.findElements(By.tagName("td"));
            if (cols.size() != columns.size()){
                continue;
            }

            costCenter.costCenter =  cols.get(1).getText().strip();

            CostCenter oldCostCenterData = conversions.checkCostCenterExistence(oldCostCenters, cols.get(1).getText().strip(), true);

            if (!oldCostCenterData.costCenter.equals("")){
                costCenter = oldCostCenterData;
            }

            costCenters.add(costCenter);
        }
    }

    @GetMapping("/invoices/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ApprovedInvoices_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> invoicesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,
                false);

        InvoicesExcelExporter excelExporter = new InvoicesExcelExporter(invoicesList);

        excelExporter.export(response);
    }
}

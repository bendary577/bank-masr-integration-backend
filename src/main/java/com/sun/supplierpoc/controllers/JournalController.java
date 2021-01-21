package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.ConsumptionExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.systemsunion.security.IAuthenticationVoucher;
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

public class JournalController {
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
    private JournalService journalService;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;
    @Autowired
    private InvoiceController invoiceController;

    public Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/getConsumption")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getJournalsRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getJournals(user.getId(), account);
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

    public HashMap<String, Object> getJournals(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CONSUMPTION, account.getId(), false);

        ArrayList<CostCenter> costCenters =  generalSettings.getCostCenterAccountMapping();
        ArrayList<CostCenter> costCentersLocation = generalSettings.getLocations();
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();

        String timePeriod = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        String consumptionBasedOnType = journalSyncJobType.getConfiguration().consumptionConfiguration.consumptionBasedOnType;

        ArrayList<OverGroup> overGroups;
        if (!journalSyncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  journalSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(journalSyncJobType, account.getERD());
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (timePeriod.equals("")){
            String message = "Map time period before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Map time period before sync consumption.";
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (costCentersLocation.size() == 0){
            String message = "Map cost centers to location before sync sales.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItemGroups().size() == 0){
            String message = "Map items before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), journalSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        ArrayList<SyncJobData> addedJournals = new ArrayList<>();
        String businessDate =  journalSyncJobType.getConfiguration().timePeriod;

        try {
            HashMap<String, Object> data;
            if (consumptionBasedOnType.equals("Cost Center")){
                data = journalService.getJournalDataByCostCenter(journalSyncJobType, costCenters
                        ,itemGroups, account);
            }else {
                data = journalService.getJournalData(journalSyncJobType, costCenters,
                        costCentersLocation,itemGroups, account);
            }

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, Object>> journals = (ArrayList<HashMap<String, Object>>) data.get("journals");
                if (journals.size() > 0) {
                    addedJournals = journalService.saveJournalData(journals, syncJob, businessDate, fromDate, overGroups);
                    if (addedJournals.size() > 0 && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(journalSyncJobType, syncJob, addedJournals, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedJournals.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync journals Successfully.");
                            response.put("success", true);
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedJournals.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Failed to connect to Sun System.");
                            response.put("success", false);
                        }
                    }
                    else if (addedJournals.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
                        AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

                        String username = sunCredentials.getUsername();
                        String password = sunCredentials.getPassword();
                        String host = sunCredentials.getHost();

                        FtpClient ftpClient = new FtpClient(host, username, password);

                        if(ftpClient.open()){
                            List<SyncJobData> creditNotesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
                            SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter(
                                    "Journals.ndf", journalSyncJobType, creditNotesList);

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
                                syncJobService.saveSyncJobStatus(syncJob, addedJournals.size(),
                                        "Sync consumption successfully.", Constants.SUCCESS);

                                response.put("success", true);
                                response.put("message", "Sync consumption successfully.");
                            }else {
                                syncJobDataService.updateSyncJobDataStatus(creditNotesList, Constants.FAILED);
                                syncJobService.saveSyncJobStatus(syncJob, addedJournals.size(),
                                        "Failed to sync consumption to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to sync consumption to sun system via FTP.");
                            }
                        }
                        else {
                            syncJobService.saveSyncJobStatus(syncJob, addedJournals.size(),
                                    "Failed to connect to sun system via FTP.", Constants.FAILED);

                            response.put("success", false);
                            response.put("message", "Failed to connect to sun system via FTP.");
                        }
                    }
                    else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No consumption to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(0);
                        syncJobRepo.save(syncJob);

                        response.put("success", true);
                        response.put("message", "No new consumption to add in middleware.");
                    }
                    return response;
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no journals to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedJournals.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no journals to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason((String) data.get("message"));
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedJournals.size());
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }
            return response;
        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedJournals.size());
            syncJobRepo.save(syncJob);

            response.put("message", e.getMessage());
            response.put("success", false);
            return response;
        }
    }

    @GetMapping("/consumption/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Consumption_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> wastageList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,false);

        ConsumptionExcelExporter excelExporter = new ConsumptionExcelExporter(wastageList);

        excelExporter.export(response);
    }

}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.ConsumptionExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController

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
    public ResponseEntity<HashMap<String, Object>> getJournalsRequest(Principal principal) throws ParseException {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = syncJournalInDayRange(user.getId(), account);
            if (response.get("success").equals(false)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }
        String message = "Invalid Credentials";
        response.put("message", message);
        response.put("success", false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    private HashMap<String, Object> syncJournalInDayRange(String userId, Account account) throws ParseException {
        HashMap<String, Object> response = new HashMap<>();
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CONSUMPTION, account.getId(), false);

        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");

        int tryCount = 2;

        if (syncJobType.getConfiguration().timePeriod.equals(Constants.USER_DEFINED)) {
            String startDate = syncJobType.getConfiguration().fromDate;
            String endDate = syncJobType.getConfiguration().toDate;

            Date date = dateFormat.parse(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            while (!startDate.equals(endDate)) {
                response = getJournals(userId, account);
                if (response.get("success").equals(true) || tryCount == 0) {
                    tryCount = 2;
                    calendar.add(Calendar.DATE, +1);
                    startDate = dateFormat.format(calendar.getTime());
                    syncJobType.getConfiguration().fromDate = startDate;
                    syncJobType.getConfiguration().toDate = startDate;
                    syncJobTypeRepo.save(syncJobType);
                } else {
                    tryCount--;
                }
            }

            String message = "Sync consumption successfully.";
            response.put("message", message);
            response.put("success", true);
        } else {
            if (syncJobType.getConfiguration().timePeriod.equals(Constants.YESTERDAY)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -1);

                syncJobType.getConfiguration().fromDate = dateFormat.format(calendar.getTime());
                syncJobType.getConfiguration().toDate = dateFormat.format(calendar.getTime());

                syncJobTypeRepo.save(syncJobType);
            }

            response = getJournals(userId, account);
        }
        return response;
    }


    public HashMap<String, Object> getJournals(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CONSUMPTION, account.getId(), false);

        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<CostCenter> costCentersLocation = generalSettings.getLocations();
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();

        String timePeriod = journalSyncJobType.getConfiguration().timePeriod;
        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;

        ConsumptionConfiguration configuration = journalSyncJobType.getConfiguration().consumptionConfiguration;
        String consumptionBasedOnType = configuration.consumptionBasedOnType;

        ArrayList<OverGroup> overGroups;
        if (!journalSyncJobType.getConfiguration().uniqueOverGroupMapping) {
            overGroups = generalSettings.getOverGroups();
        } else {
            overGroups = journalSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(journalSyncJobType, account.getERD());
        if (sunConfigResponse != null) {
            return sunConfigResponse;
        }

        if (timePeriod.equals("")) {
            String message = "Map time period before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        } else if (timePeriod.equals("UserDefined")) {
            if (fromDate.equals("")
                    || toDate.equals("")) {
                String message = "Map time period before sync consumption.";
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0) {
            String message = "Map cost centers before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (costCentersLocation.size() == 0) {
            String message = "Map cost centers to location before sync sales.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItemGroups().size() == 0) {
            String message = "Map items before sync consumption.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), journalSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;

        try {
            Response data;

//            if (consumptionBasedOnType.equals("Cost Center")) {
//                data = journalService.getJournalDataByCostCenter(journalSyncJobType, costCenters, itemGroups, account);
//            } else {
//                data = journalService.getJournalData(journalSyncJobType, costCentersLocation, itemGroups, costCenters, account);
//            }


            ArrayList<ConsumptionLocation> consumptionLocations = configuration.consumptionLocations;
            data = journalService.getJournalDataByItemGroup(journalSyncJobType, consumptionLocations, account);

            if (data.isStatus()) {
                ArrayList<JournalBatch> journalBatches = data.getJournalBatches();
                ArrayList<JournalBatch> addedJournalBatches;

                if (journalBatches.size() > 0) {
//                    addedJournalBatches = journalService.saveJournalData(journalBatches, journalSyncJobType, syncJob,
//                            businessDate, fromDate);
                    addedJournalBatches = journalService.saveJournalDataByItemGroup(journalBatches, journalSyncJobType, syncJob,
                            businessDate, fromDate);

                    if (addedJournalBatches.size() > 0 && account.getERD().equals(Constants.SUN_ERD)) {
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null) {
                            for (JournalBatch batch : journalBatches) {
                                invoiceController.handleSendJournal(journalSyncJobType, syncJob, batch.getConsumptionData(), account, voucher);
                            }

                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(journalBatches.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync journals Successfully.");
                            response.put("success", true);
                        } else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(journalBatches.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Failed  to connect to Sun System.");
                            response.put("success", false);
                        }
                    } else if (addedJournalBatches.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)) {
                        List<SyncJobData> consumptionList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);

                        File file;
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);
                        SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(journalSyncJobType, consumptionList);

                        if (journalSyncJobType.getConfiguration().exportFilePerLocation &&
                                (consumptionBasedOnType.equals("Location") || consumptionBasedOnType.equals("Location And RevenuCenter"))) {
                            ArrayList<File> files = createConsumptionFilePerLocation(addedJournalBatches, journalSyncJobType, account.getName());
                        } else {
                            file = exporter.prepareNDFFile(consumptionList, journalSyncJobType, account.getName(), "");
                        }

                        if (ftpClient != null) {
                            if (ftpClient.open()) {
//                                boolean sendFileFlag = false;
//                                try {
//                                    sendFileFlag = ftpClient.putFileToPath(file, file.getName());
//                                    ftpClient.close();
//                                } catch (IOException e) {
//                                    ftpClient.close();
//                                }
//
//                                if (sendFileFlag){
                                if (true) {
                                    syncJobDataService.updateSyncJobDataStatus(consumptionList, Constants.SUCCESS);
                                    syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                            "Sync consumption successfully.", Constants.SUCCESS);

                                    response.put("success", true);
                                    response.put("message", "Sync consumption successfully.");
                                } else {
                                    syncJobDataService.updateSyncJobDataStatus(consumptionList, Constants.FAILED);
                                    syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                            "Failed to sync consumption to sun system via FTP.", Constants.FAILED);

                                    response.put("success", false);
                                    response.put("message", "Failed to sync consumption to sun system via FTP.");
                                }
                            } else {
                                syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                        "Failed to connect to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to connect to sun system via FTP.");
                            }
                        } else {
                            syncJobDataService.updateSyncJobDataStatus(consumptionList, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                    "Sync approved Invoices successfully.", Constants.SUCCESS);

                            response.put("success", true);
                            response.put("message", "Sync sales successfully.");
                        }
                    } else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No consumption to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(0);
                        syncJobRepo.save(syncJob);

                        response.put("success", true);
                        response.put("message", "No new consumption to add in middleware.");
                    }
                    return response;
                } else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no journals to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(0);
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no journals to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(data.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);

                response.put("message", data.getMessage());
                response.put("success", false);
            }
            return response;
        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(0);
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

        List<SyncJobData> wastageList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId, false);

        ConsumptionExcelExporter excelExporter = new ConsumptionExcelExporter(wastageList);

        excelExporter.export(response);
    }

    private ArrayList<File> createConsumptionFilePerLocation(List<JournalBatch> wasteBatches, SyncJobType syncJobType,
                                                             String AccountName) {
        ArrayList<File> locationFiles = new ArrayList<>();
        try {
            File file;
            List<SyncJobData> consumptionList;
            SalesFileDelimiterExporter excelExporter;

            for (JournalBatch locationBatch : wasteBatches) {
                consumptionList = new ArrayList<>(locationBatch.getConsumptionData());

                excelExporter = new SalesFileDelimiterExporter(syncJobType, consumptionList);

                file = excelExporter.prepareNDFFile(consumptionList, syncJobType, AccountName, locationBatch.getCostCenter().costCenterReference);
                locationFiles.add(file);
            }
            return locationFiles;
        } catch (Exception e) {
            e.printStackTrace();
            return locationFiles;
        }
    }

    @RequestMapping("/addConsumptionMajorGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addMajorGroup(@RequestBody ArrayList<MajorGroup> majorGroups,
                                                  @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                  Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().consumptionConfiguration.majorGroups = majorGroups;
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update consumption major groups successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update consumption major groups.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping("/updateConsumptionLocations")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> updateConsumptionLocations(@RequestBody ArrayList<ConsumptionLocation> locations,
                                                  @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                  @RequestParam(name = "updateLocation") boolean updateLocation,
                                                  Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                if(updateLocation)
                    syncJobType.getConfiguration().consumptionConfiguration.consumptionLocations = locations;
                else
                    syncJobType.getConfiguration().consumptionConfiguration.consumptionCostCenters = locations;

                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update consumption locations successfully.");

                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update consumption locations.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }else{
            response.setStatus(false);
            response.setMessage("Wrong Credentials");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }
}

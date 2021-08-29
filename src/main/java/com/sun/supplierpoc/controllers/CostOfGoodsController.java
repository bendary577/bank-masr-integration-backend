package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class CostOfGoodsController {
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
    private CostOfGoodsService costOfGoodsService;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;
    @Autowired
    private InvoiceController invoiceController;
    @Autowired
    private ImageService imageService;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/getCostOfGoods")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity getCostOfGoodsRequest(Principal principal) {
        String message = "";
        Response response = new Response();

        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            try {
                response = syncCostOfGoodsInDayRange(user.getId(), account);
            } catch (ParseException e) {
                e.printStackTrace();

                message = e.getMessage();
                response.setStatus(false);
                response.setMessage(message);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            if (!response.isStatus()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        message = "Invalid Credentials";
        response.setStatus(false);
        response.setMessage(message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

    }

    public Response syncCostOfGoodsInDayRange(String userId, Account account) throws ParseException {
        Response response = new Response();
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.COST_OF_GOODS, account.getId(), false);

        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");

        int tryCount = 2;

        if (syncJobType.getConfiguration().timePeriod.equals(Constants.USER_DEFINED)) {
            String startDate = syncJobType.getConfiguration().fromDate;
            String endDate = syncJobType.getConfiguration().toDate;

            Date date = dateFormat.parse(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            while (!startDate.equals(endDate)) {
                response = getCostOfGoods(userId, account);
                if (response.isStatus() || tryCount == 0) {
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

            String message = "Sync cost of goods successfully.";
            response.setStatus(true);
            response.setMessage(message);

        } else {
            if (syncJobType.getConfiguration().timePeriod.equals(Constants.YESTERDAY)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -1);

                syncJobType.getConfiguration().fromDate = dateFormat.format(calendar.getTime());
                syncJobType.getConfiguration().toDate = dateFormat.format(calendar.getTime());

                syncJobTypeRepo.save(syncJobType);
            }

            response = getCostOfGoods(userId, account);
        }
        return response;
    }

    private Response getCostOfGoods(String userId, Account account) {
        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.COST_OF_GOODS, account.getId(), false);
        ConsumptionConfiguration configuration = journalSyncJobType.getConfiguration().consumptionConfiguration;

        String fromDate = journalSyncJobType.getConfiguration().fromDate;
        String toDate = journalSyncJobType.getConfiguration().toDate;
        String timePeriod = journalSyncJobType.getConfiguration().timePeriod;

        ArrayList<CostCenter> costCentersLocation = generalSettings.getLocations();
        ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();
        ArrayList<MajorGroup> majorGroups = configuration.majorGroups;

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(journalSyncJobType, account.getERD());
        if (sunConfigResponse != null) {
            response.setStatus(false);
            response.setMessage((String) sunConfigResponse.get("message"));

            return response;
        }

        if (timePeriod.equals("")) {
            String message = "Map time period before sync cost of goods.";
            response.setStatus(false);
            response.setMessage(message);
            return response;
        } else if (timePeriod.equals("UserDefined")) {
            if (fromDate.equals("")
                    || toDate.equals("")) {
                String message = "Map time period before sync cost of goods.";
                response.setStatus(false);
                response.setMessage(message);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0) {
            String message = "Map cost centers before sync cost of goods.";
            response.setStatus(false);
            response.setMessage(message);
            return response;
        }

        if (costCentersLocation.size() == 0) {
            String message = "Map cost centers to location before sync sales.";
            response.setStatus(false);
            response.setMessage(message);
            return response;
        }

        if (generalSettings.getItemGroups().size() == 0) {
            String message = "Map items before sync cost of goods.";
            response.setStatus(false);
            response.setMessage(message);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), journalSyncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        String businessDate = journalSyncJobType.getConfiguration().timePeriod;

        try {
            Response data;

            if(account.getMicrosVersion().equals("version1")) {
                data = costOfGoodsService.getJournalDataByRevenueCenter(journalSyncJobType, costCentersLocation, majorGroups, revenueCenters, account);
            }else{
                data = costOfGoodsService.getJournalDataByRevenueCenterVersion2(journalSyncJobType, costCentersLocation, majorGroups, revenueCenters, account);
            }

            if (data.isStatus()) {
                ArrayList<JournalBatch> journalBatches = data.getJournalBatches();
                ArrayList<JournalBatch> addedJournalBatches;

                if (journalBatches.size() > 0) {
                    addedJournalBatches = costOfGoodsService.saveCostOfGoodsData(journalBatches, journalSyncJobType, syncJob,
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

                            response.setStatus(true);
                            response.setMessage("Sync cost of goods Successfully.");
                        } else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(journalBatches.size());
                            syncJobRepo.save(syncJob);

                            response.setStatus(false);
                            response.setMessage("Failed  to connect to Sun System.");
                        }
                    } else if (addedJournalBatches.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)) {
                        List<SyncJobData> consumptionList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);

                        File file;
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);
                        SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(journalSyncJobType, consumptionList);

                        if (journalSyncJobType.getConfiguration().exportFilePerLocation) {
                            ArrayList<File> files = createConsumptionFilePerLocation(addedJournalBatches, journalSyncJobType, account.getName());
                            for (File f : files) {
                                imageService.storeFile(f);
                            }
                        } else {
                            file = exporter.prepareNDFFile(consumptionList, journalSyncJobType, account.getName(), "");
                            if(file != null)
                                imageService.storeFile(file);
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
                                            "Sync cost of goods successfully.", Constants.SUCCESS);

                                    response.setStatus(true);
                                    response.setMessage("Sync cost of goods successfully.");
                                } else {
                                    syncJobDataService.updateSyncJobDataStatus(consumptionList, Constants.FAILED);
                                    syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                            "Failed to sync cost of goods to sun system via FTP.", Constants.FAILED);

                                    response.setStatus(false);
                                    response.setMessage("Failed to sync cost of goods to sun system via FTP.");
                                }
                            } else {
                                syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                        "Failed to connect to sun system via FTP.", Constants.FAILED);

                                response.setStatus(false);
                                response.setMessage("Failed to sync cost of goods to sun system via FTP.");
                            }
                        } else {
                            syncJobDataService.updateSyncJobDataStatus(consumptionList, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, consumptionList.size(),
                                    "Sync cost of goods successfully.", Constants.SUCCESS);

                            response.setStatus(true);
                            response.setMessage("Sync cost of goods successfully.");
                        }
                    } else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No new cost of goods to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(0);
                        syncJobRepo.save(syncJob);

                        response.setStatus(true);
                        response.setMessage("No new cost of goods to add in middleware.");
                    }
                    return response;
                } else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no cost of goods to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(0);
                    syncJobRepo.save(syncJob);

                    response.setStatus(true);
                    response.setMessage("There is no cost of goods to get from Oracle Hospitality.");
                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(data.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);

                response.setStatus(false);
                response.setMessage(data.getMessage());
            }
            return response;
        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(0);
            syncJobRepo.save(syncJob);

            response.setStatus(false);
            response.setMessage(e.getMessage());

            return response;
        }
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

                file = excelExporter.prepareNDFFile(consumptionList, syncJobType, AccountName, locationBatch.getLocation().costCenterReference);
                if(file != null)
                    locationFiles.add(file);
            }
            return locationFiles;
        } catch (Exception e) {
            e.printStackTrace();
            return locationFiles;
        }
    }

}

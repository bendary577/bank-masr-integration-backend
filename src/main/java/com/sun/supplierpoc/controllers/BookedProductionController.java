package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.systemsunion.security.IAuthenticationVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
public class BookedProductionController {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private BookedProductionService bookedProductionService;
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

    @RequestMapping("/getBookedProduction")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getBookedProductionRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getBookedProduction(user.getId(), account);
        }

        return response;
    }

    public HashMap<String, Object> getBookedProduction(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType bookedProductionSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.BOOKED_PRODUCTION, account.getId(), false);

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<OverGroup> overGroups = generalSettings.getOverGroups();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();

        String timePeriod = bookedProductionSyncJobType.getConfiguration().timePeriod;
        String fromDate = bookedProductionSyncJobType.getConfiguration().fromDate;
        String toDate = bookedProductionSyncJobType.getConfiguration().toDate;

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(bookedProductionSyncJobType, account.getERD());
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (timePeriod.equals("")){
            String message = "Map time period before sync booked production.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Map time period before sync booked production.";
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync booked production.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItems().size() == 0){
            String message = "Map items before sync booked production.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getOverGroups().size() == 0){
            String message = "Map over groups before sync booked production.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, userId,
                account.getId(), bookedProductionSyncJobType.getId(), 0);
        syncJobRepo.save(syncJob);
        ArrayList<SyncJobData> addedBookedProduction = new ArrayList<>();
        try {
            Response data;
            ArrayList<BookedProduction> bookedProduction ;

            data = bookedProductionService.getBookedProductionData(bookedProductionSyncJobType, costCenters,
                    items, overGroups,account);
            bookedProduction = data.getBookedProduction();

            if (data.isStatus()){
                if (bookedProduction.size() > 0){
                    addedBookedProduction = bookedProductionService.saveBookedProductionData(bookedProduction,
                            syncJob, bookedProductionSyncJobType);
                    if (addedBookedProduction.size() > 0 && account.getERD().equals(Constants.SUN_ERD)){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(bookedProductionSyncJobType, syncJob,
                                    addedBookedProduction, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedBookedProduction.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync booked production Successfully.");
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedBookedProduction.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Failed to connect to Sun System.");
                            response.put("success", false);
                            return response;
                        }
                    }
                    else if (addedBookedProduction.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                        FtpClient ftpClient = new FtpClient();
                        ftpClient = ftpClient.createFTPClient(account);
                        SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(bookedProductionSyncJobType, addedBookedProduction);
                        File file = exporter.prepareNDFFile(addedBookedProduction, bookedProductionSyncJobType, account.getName(), "");

                        if(file != null && ftpClient != null){
                            if(ftpClient.open()){
                                boolean sendFileFlag = false;
                                try {
                                    sendFileFlag = ftpClient.putFileToPath(file, file.getName());
                                    ftpClient.close();
                                } catch (IOException e) {
                                    ftpClient.close();
                                }

                                if (sendFileFlag){
//                            if (true){
                                    syncJobDataService.updateSyncJobDataStatus(addedBookedProduction, Constants.SUCCESS);
                                    syncJobService.saveSyncJobStatus(syncJob, addedBookedProduction.size(),
                                            "Sync booked production successfully.", Constants.SUCCESS);

                                    response.put("success", true);
                                    response.put("message", "Sync booked production successfully.");
                                }else {
                                    syncJobDataService.updateSyncJobDataStatus(addedBookedProduction, Constants.FAILED);
                                    syncJobService.saveSyncJobStatus(syncJob, addedBookedProduction.size(),
                                            "Failed to sync booked production to sun system via FTP.", Constants.FAILED);

                                    response.put("success", false);
                                    response.put("message", "Failed to sync booked production to sun system via FTP.");
                                }
                            }
                            else {
                                syncJobService.saveSyncJobStatus(syncJob, addedBookedProduction.size(),
                                        "Failed to connect to sun system via FTP.", Constants.FAILED);

                                response.put("success", false);
                                response.put("message", "Failed to connect to sun system via FTP.");
                            }
                        }else{
                            syncJobDataService.updateSyncJobDataStatus(addedBookedProduction, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, addedBookedProduction.size(),
                                    "Sync approved Invoices successfully.", Constants.SUCCESS);

                            response.put("success", true);
                            response.put("message", "Sync sales successfully.");
                        }
                    }

                    else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No new booked production to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(addedBookedProduction.size());
                        syncJobRepo.save(syncJob);

                        response.put("message", "No new booked production to add in middleware.");
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no booked production to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedBookedProduction.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no booked production to get from Oracle Hospitality.");

                }
                response.put("success", true);
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(data.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedBookedProduction.size());
                syncJobRepo.save(syncJob);

                response.put("message", "Failed to get booked production from Oracle Hospitality.");
                response.put("success", false);
            }
        }catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedBookedProduction.size());
            syncJobRepo.save(syncJob);

            response.put("message", e);
            response.put("success", false);
        }

        return response;
    }

}

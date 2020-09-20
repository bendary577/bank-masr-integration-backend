package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.JournalService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.security.IAuthenticationVoucher;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@RestController
// @RequestMapping(path = "server")

public class JournalController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private JournalService journalService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private InvoiceController invoiceController;

    public Conversions conversions = new Conversions();
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/getConsumption")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getJournalsRequest(Principal principal) {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> response = getJournals(user.getId(), account);

        return response;

    }

    public HashMap<String, Object> getJournals(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CONSUMPTION, account.getId(), false);

        ArrayList<CostCenter> costCenters =  generalSettings.getCostCenterAccountMapping();
        ArrayList<CostCenter> costCentersLocation = generalSettings.getCostCenterLocationMapping();
        ArrayList<ItemGroup> itemGroups = generalSettings.getItemGroups();

        String timePeriod = journalSyncJobType.getConfiguration().getTimePeriod();
        String fromDate = journalSyncJobType.getConfiguration().getFromDate();
        String toDate = journalSyncJobType.getConfiguration().getToDate();

        ArrayList<OverGroup> overGroups;
        if (!journalSyncJobType.getConfiguration().getUniqueOverGroupMapping()){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  journalSyncJobType.getConfiguration().getOverGroups();
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(journalSyncJobType);
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
        String businessDate =  journalSyncJobType.getConfiguration().getTimePeriod();

        try {
            HashMap<String, Object> data = journalService.getJournalData(journalSyncJobType, costCenters,
                    costCentersLocation,itemGroups, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, Object>> journals = (ArrayList<HashMap<String, Object>>) data.get("journals");
                if (journals.size() > 0) {
                    addedJournals = journalService.saveJournalData(journals, syncJob, businessDate, fromDate, overGroups);
                    if (addedJournals.size() > 0){
                        IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
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
                    }else {
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

}

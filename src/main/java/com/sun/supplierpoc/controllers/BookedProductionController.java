package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.BookedProductionService;
import com.sun.supplierpoc.services.InvoiceService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.security.IAuthenticationVoucher;
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
    private BookedProductionService bookedProductionService;
    @Autowired
    private TransferService transferService;
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

        ArrayList<OverGroup> overGroups = generalSettings.getOverGroups();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();

        String timePeriod = bookedProductionSyncJobType.getConfiguration().getTimePeriod();
        String fromDate = bookedProductionSyncJobType.getConfiguration().getFromDate();
        String toDate = bookedProductionSyncJobType.getConfiguration().getToDate();

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(bookedProductionSyncJobType);
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

            data = bookedProductionService.getBookedProductionData(bookedProductionSyncJobType,
                    costCenters, account);
            bookedProduction = data.getBookedProduction();

            if (data.isStatus()){
                if (bookedProduction.size() > 0){
                    addedBookedProduction = bookedProductionService.saveBookedProductionData(bookedProduction,overGroups,
                            syncJob, bookedProductionSyncJobType);
                    if (addedBookedProduction.size() > 0){
                        IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
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
                syncJob.setReason("Failed to get booked production from Oracle hospitality.");
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

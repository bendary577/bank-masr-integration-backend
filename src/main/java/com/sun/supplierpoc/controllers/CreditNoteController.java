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
// @RequestMapping(path = "server")

public class CreditNoteController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private InvoiceController invoiceController;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getCreditNotes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCreditNotesRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getCreditNotes(user.getId(), account);
        }

        return response;
    }

    public HashMap<String, Object> getCreditNotes(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType creditNoteSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CREDIT_NOTES, account.getId(), false);
        SyncJobType invoiceSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.APPROVED_INVOICES, account.getId(), false);

        String invoiceTypeIncluded = invoiceSyncJobType.getConfiguration().getInvoiceTypeIncluded();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<Item> items =  generalSettings.getItems();

        ArrayList<OverGroup> overGroups ;
        if (!invoiceSyncJobType.getConfiguration().getUniqueOverGroupMapping()){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  invoiceSyncJobType.getConfiguration().getOverGroups();
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(invoiceSyncJobType);
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (invoiceTypeIncluded.equals("")){
            String message = "Configure invoice types before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (creditNoteSyncJobType.getConfiguration().getTimePeriod().equals("")){
            String message = "Map time period before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync invoices.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (generalSettings.getItems().size() == 0){
            String message = "Map items before sync credit notes.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, userId,
                account.getId(), creditNoteSyncJobType.getId(), 0);
        syncJobRepo.save(syncJob);
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();
        try {
            HashMap<String, Object> data;
            ArrayList<HashMap<String, String>> invoices ;

            if (invoiceTypeIncluded.equals(Constants.APPROVED_INVOICE)){
                data = invoiceService.getInvoicesData(false,1, invoiceSyncJobType, costCenters,
                        items, overGroups, account);
            }
            else if (invoiceTypeIncluded.equals(Constants.ACCOUNT_PAYABLE)){
                data = invoiceService.getInvoicesData(false, 2, invoiceSyncJobType, costCenters,
                        items, overGroups, account);
            }
            else{
                data = invoiceService.getInvoicesData(false,3, invoiceSyncJobType, costCenters,
                        items, overGroups, account);
            }
            invoices = (ArrayList<HashMap<String, String>>) data.get("invoices");

            if (data.get("status").equals(Constants.SUCCESS)){
                if (invoices.size() > 0){
                    addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, creditNoteSyncJobType, true);
                    if (addedInvoices.size() > 0){
                        IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(invoiceSyncJobType, syncJob, addedInvoices, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedInvoices.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Sync credit notes Successfully.");
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedInvoices.size());
                            syncJobRepo.save(syncJob);

                            response.put("message", "Failed to connect to Sun System.");
                            response.put("success", false);
                            return response;
                        }
                    }
                    else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No new credit notes to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(addedInvoices.size());
                        syncJobRepo.save(syncJob);

                        response.put("message", "No new credit notes to add in middleware.");
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no credit notes to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(addedInvoices.size());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no credit notes to get from Oracle Hospitality.");

                }
                response.put("success", true);
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason((String) data.get("message"));
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedInvoices.size());
                syncJobRepo.save(syncJob);

                response.put("message", "Failed to get credit notes from Oracle Hospitality.");
                response.put("success", false);
            }
        }catch (Exception e) {
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

}

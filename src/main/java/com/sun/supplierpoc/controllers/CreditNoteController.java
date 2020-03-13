package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
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

public class CreditNoteController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private InvoiceController invoiceController;

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

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.CREDIT_NOTES, account.getId());
        SyncJobType journalSyncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.CONSUMPTION, account.getId());
        SyncJobType invoiceSyncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, account.getId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, userId,
                account.getId(), syncJobType.getId());
        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = invoiceService.getInvoicesData(true, invoiceSyncJobType, account);

        if (data.get("status").equals(Constants.SUCCESS)){
            ArrayList<HashMap<String, Object>> invoices = (ArrayList<HashMap<String, Object>>) data.get("invoices");
            if (invoices.size() > 0){
                ArrayList<SyncJobData> addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, true);
                if (addedInvoices.size() > 0){
                    IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                    if (voucher != null){
                        invoiceController.handleSendJournal(invoiceSyncJobType, journalSyncJobType, syncJob, addedInvoices, account, voucher);
                        syncJob.setReason("");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);

                        response.put("message", "Sync credit notes Successfully.");
                    }
                    else {
                        syncJob.setStatus(Constants.FAILED);
                        syncJob.setReason("Failed to connect to Sun System.");
                        syncJob.setEndDate(new Date());
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
                    syncJobRepo.save(syncJob);

                    response.put("message", "No new credit notes to add in middleware.");
                }
            }
            else {
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setReason("There is no credit notes to get from Oracle Hospitality.");
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                response.put("message", "There is no credit notes to get from Oracle Hospitality.");

            }
            response.put("success", true);
        }
        else {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason((String) data.get("message"));
            syncJob.setEndDate(new Date());
            syncJobRepo.save(syncJob);

            response.put("message", "Failed to get credit notes from Oracle Hospitality.");
            response.put("success", false);
        }
        return response;
    }

}

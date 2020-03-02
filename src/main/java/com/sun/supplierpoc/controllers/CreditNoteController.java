package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.InvoiceService;
import com.sun.supplierpoc.services.TransferService;
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
public class CreditNoteController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private TransferService transferService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getCreditNotes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCreditNotes(Principal principal) {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();

        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.CREDIT_NOTES, user.getAccountId());
        SyncJobType syncJobTypeApprovedInvoices = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, user.getAccountId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, user.getId(),
                user.getAccountId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = invoiceService.getInvoicesData(true, syncJobTypeApprovedInvoices, account);

        if (data.get("status").equals(Constants.SUCCESS)){
            ArrayList<HashMap<String, Object>> invoices = (ArrayList<HashMap<String, Object>>) data.get("invoices");

            if (invoices.size() > 0){
                ArrayList<SyncJobData> addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, true);
                if(addedInvoices.size() != 0){
                    try {
                        for (SyncJobData invoice: addedInvoices ) {
                            data  = transferService.sendTransferData(invoice, syncJobType);
                            if ((Boolean) data.get("status")){
                                invoice.setStatus(Constants.SUCCESS);
                                invoice.setReason("");
                                syncJobDataRepo.save(invoice);
                            }
                            else {
                                invoice.setStatus(Constants.FAILED);
                                invoice.setReason((String) data.get("message"));
                                syncJobDataRepo.save(invoice);
                            }
                        }
                    } catch (SoapFaultException | ComponentException e) {
                        e.printStackTrace();
                    }
                }

                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJobRepo.save(syncJob);

                return response;
            }
            else {
                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setReason("There is no credit note to get from Oracle Hospitality.");
                syncJob.setEndDate(new Date());

                syncJobRepo.save(syncJob);

                response.put("message", "There is no credit note to get from Oracle Hospitality.");
                response.put("success", true);
                return response;
            }
        }
        else {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason("Failed to get credit note from Oracle Hospitality.");
            syncJob.setEndDate(new Date());

            syncJobRepo.save(syncJob);

            response.put("message", "Failed to sync credit note.");
            response.put("success", false);
            return response;
        }
    }

}

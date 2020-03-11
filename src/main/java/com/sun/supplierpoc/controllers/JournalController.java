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
import com.sun.supplierpoc.services.JournalService;
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
    private JournalService journalService;
    @Autowired
    private TransferService transferService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/getJournals")
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

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.JOURNALS, account.getId());
        SyncJobType syncJobTypeApprovedInvoice = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, account.getId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        try {
            HashMap<String, Object> data = journalService.getJournalData(syncJobType, syncJobTypeApprovedInvoice, account);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, Object>> journals = (ArrayList<HashMap<String, Object>>) data.get("journals");
                if (journals.size() > 0) {
                    ArrayList<SyncJobData> addedJournals = journalService.saveJournalData(journals, syncJob);
                    if (addedJournals.size() != 0) {
                        for (SyncJobData addedJournal : addedJournals) {
                            try {
                                data  = transferService.sendTransferData(addedJournal, syncJobType, syncJobType, account);
                                if ((Boolean) data.get("status")){
                                    addedJournal.setStatus(Constants.SUCCESS);
                                    addedJournal.setReason("");
                                    syncJobDataRepo.save(addedJournal);
                                }
                                else {
                                    addedJournal.setStatus(Constants.FAILED);
                                    addedJournal.setReason((String) data.get("message"));
                                    syncJobDataRepo.save(addedJournal);
                                }

                            } catch (SoapFaultException | ComponentException e) {
                                e.printStackTrace();
                            }
                        }

                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);
                    }
                    else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("There is no journals to save from Oracle Hospitality.");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);
                    }
                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no journals to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no journals to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason((String) data.get("message"));
                syncJob.setEndDate(new Date());

                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }
            return response;

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());

            syncJobRepo.save(syncJob);
            response.put("message", e);
            response.put("success", false);
            return response;
        }

    }


}

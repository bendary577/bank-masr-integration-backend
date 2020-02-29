package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
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

@RestController

public class JournalController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private JournalService journalService;
    @Autowired
    private TransferService transferService;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @RequestMapping("/getJournals")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getJournals(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(Constants.JOURNALS, user.getAccountId());
        SyncJobType syncJobTypeApprovedInvoice = syncJobTypeRepo.findByNameAndAccountId(Constants.APPROVED_INVOICES, user.getAccountId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, user.getId(),
                user.getAccountId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        try {
            HashMap<String, Object> data = journalService.getJournalData(syncJobType, syncJobTypeApprovedInvoice);

            if (data.get("status").equals(Constants.SUCCESS)) {
                ArrayList<HashMap<String, String>> journals = (ArrayList<HashMap<String, String>>) data.get("journals");
                if (journals.size() > 0) {
                    ArrayList<SyncJobData> addedJournals = journalService.saveJournalData(journals, syncJob);
                    if (addedJournals.size() != 0) {
                        for (SyncJobData addedJournal : addedJournals) {
                            try {
                                boolean sendFlag = transferService.sendTransferData(addedJournal, syncJobType);
                                if (sendFlag){
                                    addedJournal.setStatus(Constants.SUCCESS);
                                    addedJournal.setReason("");
                                    syncJobDataRepo.save(addedJournal);
                                }
                                else {
                                    addedJournal.setStatus(Constants.FAILED);
                                    addedJournal.setReason("Failed to send journal to sun system.");
                                    syncJobDataRepo.save(addedJournal);
                                }

                            } catch (SoapFaultException | ComponentException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else {
                        syncJob.setStatus(Constants.FAILED);
                        syncJob.setReason("Failed to add journals in middleware");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);
                    }

                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setEndDate(new Date());
                    syncJobRepo.save(syncJob);

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

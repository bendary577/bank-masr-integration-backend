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
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Journals", user.getAccountId());

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, user.getId(),
                user.getAccountId(), syncJobType.getId());

        syncJobRepo.save(syncJob);

        try {
            HashMap<String, Object> data = journalService.getJournalData(syncJobType);

            if (data.get("status").equals(Constants.SUCCESS)){
                ArrayList<HashMap<String, String>> journals = (ArrayList<HashMap<String, String>>) data.get("journals");
                if (journals.size() > 0){
                    ArrayList<SyncJobData> addedJournals = journalService.saveJournalData(journals, syncJob);
                    if(addedJournals.size() != 0){
                        try {
                            sendJournalData(syncJobType, addedJournals, transferService, syncJobDataRepo);
                            syncJob.setStatus(Constants.SUCCESS);
                            syncJob.setEndDate(new Date());
                            syncJobRepo.save(syncJob);

                        } catch (SoapFaultException | ComponentException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        syncJob.setStatus(Constants.FAILED);
                        syncJob.setReason("Failed to add journals in middle ware.");
                        syncJob.setEndDate(new Date());
                        syncJobRepo.save(syncJob);
                    }

                }
                else {
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no journals to get from Oracle Hospitality.");
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no journals to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get journals from Oracle Hospitality.");
                syncJobRepo.save(syncJob);

                response.put("message", data.get("message"));
                response.put("success", false);
            }
            return response;

        }
        catch (Exception e){
            response.put("message", e);
            response.put("success", false);
            return response;
        }
    }

    static void sendJournalData(SyncJobType syncJobType, ArrayList<SyncJobData> addedJournals, TransferService transferService, SyncJobDataRepo syncJobDataRepo) throws SoapFaultException, ComponentException {
        for (SyncJobData invoice: addedJournals) {
            boolean addTransferFlag = transferService.sendTransferData(invoice, syncJobType);

            if(addTransferFlag){
                invoice.setStatus(Constants.SUCCESS);
                invoice.setReason("");
            }
            else {
                invoice.setStatus(Constants.FAILED);
                invoice.setReason("");
            }
            syncJobDataRepo.save(invoice);
        }
    }

}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.seleniumMethods.SetupEnvironment;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;




@RestController

public class TransferController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private TransferService transferService;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getBookedTransfer() {
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Booked Transfers", "1");
        SyncJobType syncJobTypeJournal = syncJobTypeRepo.findByNameAndAccountId("Journals", "1");

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        try {
            HashMap<String, Object> data = transferService.getTransferData(syncJobTypeJournal);

            if (data.get("status").equals(Constants.SUCCESS)){
                ArrayList<HashMap<String, String>> transfers = (ArrayList<HashMap<String, String>>) data.get("transfers");
                if (transfers.size() > 0){
                    ArrayList<SyncJobData> addedTransfers = transferService.saveTransferData(transfers, syncJob);
                    if(addedTransfers.size() != 0){
                        try {
                            for (SyncJobData invoice: addedTransfers ) {
                                boolean addTransferFlag = transferService.sendTransferData(invoice, syncJobType);

                                if(addTransferFlag){
                                    invoice.setStatus(Constants.SUCCESS);
                                }
                                else {
                                    invoice.setStatus(Constants.FAILED);
                                    invoice.setReason("");
                                }
                                syncJobDataRepo.save(invoice);
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
                    syncJob.setReason("There is no transfers to get from Oracle Hospitality.");
                    syncJobRepo.save(syncJob);

                    response.put("message", "There is no transfers to get from Oracle Hospitality.");
                    response.put("success", true);

                }
            }
            else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason("Failed to get transfers from Oracle Hospitality.");
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

}

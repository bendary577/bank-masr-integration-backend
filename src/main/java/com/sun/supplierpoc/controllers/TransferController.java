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
    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private TransferService transferService;

    public Conversions conversions = new Conversions();
    public Constants constant = new Constants();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getBookedTransfer")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<SyncJobData> getBookedTransfer() {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Booked Transfers", "1");

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        HashMap<String, Object> invoicesData = transferService.getTransferData(syncJobType);

        ArrayList<HashMap<String, String>> transfers = (ArrayList<HashMap<String, String>>) invoicesData.get("transfers");

        ArrayList<SyncJobData> addedTransfers = transferService.saveTransferData(transfers, syncJob);
        if (addedTransfers.size() != 0){
//            try {
//                transferService.sendTransferData(syncJobType);
//            } catch (SoapFaultException | ComponentException e) {
//                e.printStackTrace();
//            }
            System.out.println("WIP");
        }

        syncJob.setStatus(Constants.SUCCESS);
        syncJob.setEndDate(new Date());
        syncJobRepo.save(syncJob);

        return addedTransfers;
    }

}

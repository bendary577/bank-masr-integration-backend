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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreditNoteController {

    static int PORT = 8080;
    static String HOST= "192.168.1.21";

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private InvoiceController invoiceController;

    public Conversions conversions = new Conversions();
    public Constants constant = new Constants();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getCreditNotes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ArrayList<SyncJobData> getCreditNotes() {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Credit Notes", "1");

        SyncJob syncJob = new SyncJob(constant.RUNNING, "",  new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);
//
//        ArrayList<HashMap<String, Object>> invoices = invoiceController.getInvoicesData(true, syncJobType);
//        ArrayList<SyncJobData> addedInvoices = invoiceController.saveInvoicesData(invoices, syncJob, true);
        ArrayList<SyncJobData> addedInvoices = new ArrayList<>();

        syncJob.setStatus(constant.SUCCESS);
        syncJob.setEndDate(new Date());
        syncJobRepo.save(syncJob);

        return addedInvoices;
    }

}

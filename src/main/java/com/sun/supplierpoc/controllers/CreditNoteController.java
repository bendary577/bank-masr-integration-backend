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
import com.sun.supplierpoc.services.InvoiceService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.ssc.client.ComponentException;
import com.systemsunion.ssc.client.SoapFaultException;
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
    private InvoiceService invoiceService;
    @Autowired
    private TransferService transferService;

    public Conversions conversions = new Conversions();
    public Constants constant = new Constants();
    public SetupEnvironment setupEnvironment = new SetupEnvironment();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getCreditNotes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public HashMap<String, Object> getCreditNotes() {
        HashMap<String, Object> response = new HashMap<>();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId("Credit Notes", "1");

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "",  new Date(), null, "1", "1",
                syncJobType.getId());

        syncJobRepo.save(syncJob);

        HashMap<String, Object> data = invoiceService.getInvoicesData(false, syncJobType);

        if (data.get("status").equals(Constants.SUCCESS)){
            ArrayList<HashMap<String, String>> invoices = (ArrayList<HashMap<String, String>>) data.get("invoices");

            if (invoices.size() > 0){
                ArrayList<SyncJobData> addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, true);
                if(addedInvoices.size() != 0){
                    try {
                        for (SyncJobData invoice: addedInvoices ) {
                            boolean addInvoiceFlag = transferService.sendTransferData(invoice, syncJobType);

                            if(addInvoiceFlag){
                                invoice.setStatus(Constants.SUCCESS);
                                syncJobDataRepo.save(invoice);
                            }
                            else {
                                invoice.setStatus(Constants.FAILED);
                                invoice.setReason("");
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
                syncJobRepo.save(syncJob);

                response.put("message", "There is no credit note to get from Oracle Hospitality.");
                response.put("success", true);
                return response;
            }
        }
        else {
            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setReason("Failed to get credit note from Oracle Hospitality.");
            syncJobRepo.save(syncJob);

            response.put("message", "Failed to sync credit note.");
            response.put("success", false);
            return response;
        }
    }

}

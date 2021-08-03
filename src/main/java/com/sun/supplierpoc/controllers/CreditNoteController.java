package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.ItemGroup;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.sun.supplierpoc.soapModels.Supplier;
import com.systemsunion.security.IAuthenticationVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
// @RequestMapping(path = "server")

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
    private GeneralSettingsRepo generalSettingsRepo;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceController invoiceController;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getCreditNotes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> getCreditNotesRequest(Principal principal) {
        HashMap<String, Object> response = new HashMap<>();

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getCreditNotes(user.getId(), account);
            if(response.get("success").equals(false)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        String message = "Invalid Credentials";
        response.put("message", message);
        response.put("success", false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    public HashMap<String, Object> getCreditNotes(String userId, Account account) {
        HashMap<String, Object> response = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType creditNoteSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CREDIT_NOTES, account.getId(), false);
        SyncJobType invoiceSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.APPROVED_INVOICES, account.getId(), false);

        String invoiceTypeIncluded = creditNoteSyncJobType.getConfiguration().invoiceConfiguration.invoiceTypeIncluded;
        String invoiceSyncPlace = invoiceSyncJobType.getConfiguration().invoiceConfiguration.invoiceSyncPlace;
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<Item> items =  generalSettings.getItems();
        ArrayList<ItemGroup> itemGroups =  generalSettings.getItemGroups();
        ArrayList<Supplier> suppliers = generalSettings.getSuppliers();

        String timePeriod = creditNoteSyncJobType.getConfiguration().timePeriod;
        String fromDate = creditNoteSyncJobType.getConfiguration().fromDate;
        String toDate = creditNoteSyncJobType.getConfiguration().toDate;

        ArrayList<OverGroup> overGroups ;
        if (!invoiceSyncJobType.getConfiguration().uniqueOverGroupMapping){
            overGroups =  generalSettings.getOverGroups();
        }else{
            overGroups =  invoiceSyncJobType.getConfiguration().overGroups;
        }

        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(creditNoteSyncJobType, account.getERD());
        if (sunConfigResponse != null){
            return sunConfigResponse;
        }

        if (invoiceTypeIncluded.equals("")){
            String message = "Configure invoice types before sync credit notes.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (timePeriod.equals("")){
            String message = "Map time period before sync credit notes.";
            response.put("message", message);
            response.put("success", false);
            return response;
        }else if (timePeriod.equals("UserDefined")){
            if (fromDate.equals("")
                    || toDate.equals("")){
                String message = "Map time period before sync credit notes.";
                response.put("message", message);
                response.put("success", false);
                return response;
            }
        }

        if (generalSettings.getCostCenterAccountMapping().size() == 0){
            String message = "Map cost centers before sync credit notes.";
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
            ArrayList<HashMap<String, Object>> invoices ;

            int invoiceType = 3; // BOTH

            if (invoiceTypeIncluded.equals(Constants.APPROVED_INVOICE)){
                invoiceType = 1;
            }
            else if (invoiceTypeIncluded.equals(Constants.ACCOUNT_PAYABLE)){
                invoiceType = 2;
            }

            if(invoiceSyncPlace.equals("Invoice")) {
                data = invoiceService.getInvoicesData(true, invoiceType, suppliers, costCenters, invoiceSyncJobType.getConfiguration(),
                        items, itemGroups, overGroups, account, timePeriod, fromDate, toDate);
            }else {
                data = invoiceService.getInvoicesReceiptsData(true,invoiceType, creditNoteSyncJobType.getConfiguration(),
                        costCenters, suppliers, items, itemGroups, overGroups, account, timePeriod, fromDate, toDate);
            }

            invoices = (ArrayList<HashMap<String, Object>>) data.get("invoices");

            if (data.get("status").equals(Constants.SUCCESS)){
                if (invoices.size() > 0  && account.getERD().equals(Constants.SUN_ERD)){
                    addedInvoices = invoiceService.saveInvoicesData(invoices, syncJob, creditNoteSyncJobType, true);
                    if (addedInvoices.size() > 0){
                        IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(invoiceSyncJobType, syncJob, addedInvoices, account, voucher);

                            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                    "", syncJob.getStatus());

                            response.put("success", true);
                            response.put("message", "Sync credit notes Successfully.");
                        }
                        else {
                            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                    "Failed to connect to Sun System.", Constants.FAILED);

                            response.put("success", false);
                            response.put("message", "Failed to connect to Sun System.");
                        }
                    }
                    else {
                        syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                "No new credit notes to add in middleware.", Constants.SUCCESS);

                        response.put("success", true);
                        response.put("message", "No new credit notes to add in middleware.");
                    }
                }
                else if (addedInvoices.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                    FtpClient ftpClient = new FtpClient();
                    ftpClient = ftpClient.createFTPClient(account);
                    SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(creditNoteSyncJobType, addedInvoices);
                    File file = exporter.prepareNDFFile(addedInvoices, creditNoteSyncJobType, account.getName(), "");

                    if(file != null && ftpClient.open()){
                        boolean sendFileFlag = false;
                        try {
                            sendFileFlag = ftpClient.putFileToPath(file, file.getName());
                            ftpClient.close();
                        } catch (IOException e) {
                            ftpClient.close();
                        }

                        if (sendFileFlag){
//                            if (true){
                            syncJobDataService.updateSyncJobDataStatus(addedInvoices, Constants.SUCCESS);
                            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                    "Sync credit notes successfully.", Constants.SUCCESS);

                            response.put("success", true);
                            response.put("message", "Sync credit notes successfully.");
                        }else {
                            syncJobDataService.updateSyncJobDataStatus(addedInvoices, Constants.FAILED);
                            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                    "Failed to sync credit notes to sun system via FTP.", Constants.FAILED);

                            response.put("success", false);
                            response.put("message", "Failed to sync credit notes to sun system via FTP.");
                        }
                    }
                    else {
                        syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                                "Failed to connect to sun system via FTP.", Constants.FAILED);

                        response.put("success", false);
                        response.put("message", "Failed to connect to sun system via FTP.");
                    }
                }
                else {
                    syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                            "There is no credit notes to get from Oracle Hospitality.", Constants.SUCCESS);

                    response.put("success", true);
                    response.put("message", "There is no credit notes to get from Oracle Hospitality.");
                }
            }
            else {
                syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                        "Failed to get credit notes from Oracle hospitality.", Constants.FAILED);

                response.put("message", "Failed to get credit notes from Oracle Hospitality.");
                response.put("success", false);
            }
        }catch (Exception e) {
            syncJobService.saveSyncJobStatus(syncJob, addedInvoices.size(),
                    e.getMessage(), Constants.FAILED);

            response.put("message", e);
            response.put("success", false);
        }

        return response;
    }


}

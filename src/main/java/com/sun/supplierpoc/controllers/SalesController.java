package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.Item;
import com.sun.supplierpoc.models.configurations.OverGroup;
import com.sun.supplierpoc.models.configurations.Tender;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.SalesService;
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
public class SalesController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private SalesService salesService;
    @Autowired
    private TransferService transferService;

    @Autowired
    private InvoiceController invoiceController;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getPOSSales")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Response getPOSSalesRequest(Principal principal) {
        Response response = new Response();
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getPOSSales(user.getId(), account);
        }

        return response;
    }

    public Response getPOSSales(String userId, Account account){
        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);

        ArrayList<Tender> tenders = syncJobType.getConfiguration().getTenders();

        ArrayList<Item> items = generalSettings.getItems();
        ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
        ArrayList<CostCenter> costCentersLocation = generalSettings.getCostCenterLocationMapping();


        //////////////////////////////////////// Validation ///////////////////////////////////////////////////////////
        HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(syncJobType);
        if (sunConfigResponse != null){
            response.setMessage((String) sunConfigResponse.get("message"));
            response.setStatus(false);
            return response;
        }

        if (syncJobType.getConfiguration().getTimePeriod().equals("")){
            String message = "Configure time period before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (syncJobType.getConfiguration().getRevenue().equals("")){
            String message = "Configure revenue before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (syncJobType.getConfiguration().getCashShortagePOS().equals("")){
            String message = "Configure cash shortage account before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (syncJobType.getConfiguration().getCashSurplusPOS().equals("")){
            String message = "Configure Cash surplus account before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (tenders.size() == 0){
            String message = "Configure tenders before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (items.size() == 0){
            String message = "Map items before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (costCenters.size() == 0){
            String message = "Map cost centers before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        if (costCentersLocation.size() == 0){
            String message = "Map cost centers to location before sync sales.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        //////////////////////////////////////// End Validation ////////////////////////////////////////////////////////

        SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                account.getId(), syncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

        ArrayList<SyncJobData> addedSales = new ArrayList<>();

        try {

            Response salesResponse = salesService.getSalesData(syncJobType, costCenters, costCentersLocation,
                    items, tenders, account);

            if (salesResponse.isStatus()){
                if (salesResponse.getSalesTender().size() > 0 || salesResponse.getSalesTax().size() > 0){
                    // Save Sales Entries
                    addedSales = salesService.saveSalesData(salesResponse, syncJob, syncJobType);

                    if (addedSales.size() > 0){
                        // Sent Sales Entries
                        IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                        if (voucher != null){
                            invoiceController.handleSendJournal(syncJobType, syncJob, addedSales, account, voucher);
                            syncJob.setReason("");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedSales.size());
                            syncJobRepo.save(syncJob);

                            response.setStatus(true);
                            response.setMessage("Sync journals Successfully.");
                        }
                        else {
                            syncJob.setStatus(Constants.FAILED);
                            syncJob.setReason("Failed to connect to Sun System.");
                            syncJob.setEndDate(new Date());
                            syncJob.setRowsFetched(addedSales.size());
                            syncJobRepo.save(syncJob);

                            response.setStatus(false);
                            response.setMessage("Failed to connect to Sun System.");
                        }

                    }else {
                        syncJob.setStatus(Constants.SUCCESS);
                        syncJob.setReason("No sales to add in middleware.");
                        syncJob.setEndDate(new Date());
                        syncJob.setRowsFetched(0);
                        syncJobRepo.save(syncJob);

                        response.setStatus(true);
                        response.setMessage("No new sales to add in middleware.");
                    }
                }else{
                    syncJob.setStatus(Constants.SUCCESS);
                    syncJob.setReason("There is no sales to get from Oracle Hospitality.");
                    syncJob.setEndDate(new Date());
                    syncJob.setRowsFetched(0);
                    syncJobRepo.save(syncJob);

                    response.setStatus(true);
                    response.setMessage("There is no sales to get from Oracle Hospitality.");
                }

            }else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(salesResponse.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(addedSales.size());
                syncJobRepo.save(syncJob);

                response.setStatus(false);
                response.setMessage(salesResponse.getMessage());
            }

        } catch (Exception e) {
            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(addedSales.size());
            syncJobRepo.save(syncJob);

            response.setStatus(false);
            response.setMessage(e.getMessage());
        }
        return response;
    }

}

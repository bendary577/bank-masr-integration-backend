package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.SalesService;
import com.sun.supplierpoc.services.SyncJobService;
import com.sun.supplierpoc.services.TransferService;
import com.systemsunion.security.IAuthenticationVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

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
    private SyncJobService syncJobService;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getPOSSales")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> getPOSSalesRequest(Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = getPOSSales(user.getId(), account);
            if(!response.isStatus()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }else {
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
        }

        String message = "Invalid Credentials";
        response.setMessage(message);
        response.setStatus(false);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    private Response getPOSSales(String userId, Account account) {
        Response response = new Response();
        SyncJob syncJob = null;
        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);

            ArrayList<Tender> tenders = syncJobType.getConfiguration().getTenders();
            ArrayList<Tax> taxes = syncJobType.getConfiguration().getTaxes();
            ArrayList<MajorGroup> majorGroups = syncJobType.getConfiguration().getMajorGroups();

            ArrayList<CostCenter> costCenters = generalSettings.getCostCenterAccountMapping();
            ArrayList<CostCenter> costCentersLocation = generalSettings.getCostCenterLocationMapping();

            String timePeriod = syncJobType.getConfiguration().getTimePeriod();
            String fromDate = syncJobType.getConfiguration().getFromDate();
            String toDate = syncJobType.getConfiguration().getToDate();

            //////////////////////////////////////// Validation ///////////////////////////////////////////////////////////
            HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(syncJobType);
            if (sunConfigResponse != null) {
                response.setMessage((String) sunConfigResponse.get("message"));
                response.setStatus(false);
                return response;
            }

            if (timePeriod.equals("")) {
                String message = "Map time period before sync credit notes.";
                response.setMessage(message);
                response.setStatus(false);

                return response;
            } else if (timePeriod.equals("UserDefined")) {
                if (fromDate.equals("") || toDate.equals("")) {
                    String message = "Map time period before sync credit notes.";
                    response.setMessage(message);
                    response.setStatus(false);

                    return response;
                }
            }

            if (syncJobType.getConfiguration().getRevenue().equals("")) {
                String message = "Configure revenue before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (syncJobType.getConfiguration().getCashShortagePOS().equals("")) {
                String message = "Configure cash shortage account before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (syncJobType.getConfiguration().getCashSurplusPOS().equals("")) {
                String message = "Configure Cash surplus account before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (tenders.size() == 0) {
                String message = "Configure tenders before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (taxes.size() == 0) {
                String message = "Configure taxes before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (majorGroups.size() == 0) {
                String message = "Map major groups before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (costCenters.size() == 0) {
                String message = "Map cost centers before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (costCentersLocation.size() == 0) {
                String message = "Map cost centers to location before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            //////////////////////////////////////// End Validation ////////////////////////////////////////////////////////

            ArrayList<JournalBatch> addedSalesBatches = new ArrayList<>();

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), syncJobType.getId(), 0);

            syncJobRepo.save(syncJob);

            try {
                Response salesResponse = salesService.getSalesData(syncJobType, costCenters, costCentersLocation,
                        majorGroups, tenders, taxes, account);

                if (salesResponse.isStatus()) {
                    if (salesResponse.getJournalBatches().size() > 0) {
                        // Save Sales Entries
                        addedSalesBatches = salesService.saveSalesJournalBatchesData(salesResponse, syncJob, syncJobType);

                        if (addedSalesBatches.size() > 0) {
                            // Sent Sales Entries
                            IAuthenticationVoucher voucher = transferService.connectToSunSystem(account);
                            if (voucher != null) {
                                // Loop over batches
                                HashMap<String, Object> data;
                                try {
                                    for (JournalBatch salesJournalBatch : addedSalesBatches) {
                                        if (salesJournalBatch.getSalesMajorGroupGrossData().size() > 0
                                                || salesJournalBatch.getSalesTenderData().size() > 0
                                                || salesJournalBatch.getSalesTaxData().size() > 0) {
                                            data = salesService.sendJournalBatches(salesJournalBatch, syncJobType, account, voucher);
                                            salesService.updateJournalBatchStatus(salesJournalBatch, data);
                                        }
                                    }
                                } catch (Exception e) {
                                    syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                            "Failed to send sales entries to Sun System.", Constants.FAILED);

                                    response.setStatus(false);
                                    response.setMessage("Failed to connect to Sun System.");
                                }

                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "", Constants.SUCCESS);

                                response.setStatus(true);
                                response.setMessage("Sync sales Successfully.");
                            } else {
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Failed to connect to Sun System.", Constants.FAILED);

                                response.setStatus(false);
                                response.setMessage("Failed to connect to Sun System.");
                            }

                        } else {
                            syncJobService.saveSyncJobStatus(syncJob, 0,
                                    "No sales to add in middleware.", Constants.SUCCESS);

                            response.setStatus(true);
                            response.setMessage("No new sales to add in middleware.");
                        }
                    }
                } else {
                    syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                            salesResponse.getMessage(), Constants.FAILED);

                    response.setStatus(false);
                    response.setMessage(salesResponse.getMessage());
                }

            } catch (Exception e) {
                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                        e.getMessage(), Constants.FAILED);

                response.setStatus(false);
                response.setMessage(e.getMessage());
            }

        } catch (Exception e) {
            if (syncJob != null) {
                syncJobService.saveSyncJobStatus(syncJob, 0,
                        e.getMessage(), Constants.FAILED);
            }

            String message = "Failed to sync sales, Please try agian after few minutes.";
            response.setStatus(false);
            response.setMessage(message);
        }
        return response;
    }

    @RequestMapping("/addTender")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addTender(@RequestBody ArrayList<Tender> tenders,
                                              @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                              Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().setTenders(tenders);
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setSalesTender(tenders);
                response.setMessage("Update sales tenders successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales tenders.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @RequestMapping("/addTax")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addTax(@RequestBody ArrayList<Tax> taxes,
                                              @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                              Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().setTaxes(taxes);
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setSalesTax(taxes);
                response.setMessage("Update sales taxes successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales taxes.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping("/addMajorGroup")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addMajorGroup(@RequestBody ArrayList<MajorGroup> majorGroups,
                                                  @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                  Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().setMajorGroups(majorGroups);
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update sales major groups successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales major groups.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

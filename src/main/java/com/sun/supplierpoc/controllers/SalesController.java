package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.fileDelimiterExporters.GeneralExporterMethods;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.systemsunion.security.IAuthenticationVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class SalesController {
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
    private SalesService salesService;
    @Autowired
    private SalesV2Services salesV2Services;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;
    @Autowired
    private ImageService imageService;
    @Autowired
    FtpService ftpService;

    public Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping("/getPOSSales")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> getPOSSalesRequest(Principal principal) throws ParseException, IOException {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = syncPOSSalesInDayRange(user.getId(), account);
            if (!response.isStatus()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
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

            SalesConfiguration configuration = syncJobType.getConfiguration().salesConfiguration;
            ArrayList<Discount> discounts = configuration.discounts;
            ArrayList<Tender> tenders = configuration.tenders;
            ArrayList<Tax> taxes = configuration.taxes;
            ArrayList<MajorGroup> majorGroups = configuration.majorGroups;
            ArrayList<ServiceCharge> serviceCharges = configuration.serviceCharges;
            ArrayList<SalesStatistics> statistics = configuration.statistics;
            ArrayList<CostCenter> locations = generalSettings.getLocations();
            ArrayList<RevenueCenter> revenueCenters = generalSettings.getRevenueCenters();

            String timePeriod = syncJobType.getConfiguration().timePeriod;
            String fromDate = syncJobType.getConfiguration().fromDate;
            String toDate = syncJobType.getConfiguration().toDate;

            //////////////////////////////////////// Validation ///////////////////////////////////////////////////////////
            HashMap<String, Object> sunConfigResponse = conversions.checkSunDefaultConfiguration(syncJobType, account.getERD());
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

            if (configuration.cashShortagePOS.equals("")) {
                String message = "Configure cash shortage account before sync sales.";
                response.setMessage(message);
                response.setStatus(false);
                return response;
            }

            if (configuration.cashSurplusPOS.equals("")) {
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

            if (taxes.size() == 0 && (!configuration.syncTotalTax || configuration.totalTaxAccount.equals(""))) {
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

            //////////////////////////////////////// End Validation ////////////////////////////////////////////////////////

            ArrayList<JournalBatch> addedSalesBatches = new ArrayList<>();

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), syncJobType.getId(), 0);

            syncJobRepo.save(syncJob);

            try {
                Response salesResponse;
                if(account.getMicrosVersion().equals("version1")){
                    salesResponse = salesService.getSalesData(syncJobType, locations,
                            majorGroups, tenders, taxes, discounts, serviceCharges, revenueCenters, statistics, account);
                }else{
                    salesResponse = salesV2Services.getSalesData(syncJobType, locations,
                            majorGroups, tenders, taxes, discounts, serviceCharges, revenueCenters, statistics, account);
                }

                if (salesResponse.isStatus()) {
                    if (salesResponse.getJournalBatches().size() > 0) {
                        // Save Sales Entries
                        addedSalesBatches = salesService.saveSalesJournalBatchesData(salesResponse, syncJob,
                                syncJobType.getConfiguration(), account);

                        if (addedSalesBatches.size() > 0 && account.getERD().equals(Constants.SUN_ERD)) {
                            // Sent Sales Entries
                            IAuthenticationVoucher voucher = sunService.connectToSunSystem(account);
                            if (voucher != null) {
                                // Loop over batches
                                HashMap<String, Object> data;
                                try {
                                    for (JournalBatch salesJournalBatch : addedSalesBatches) {
                                        if (salesJournalBatch.getSalesMajorGroupGrossData().size() > 0
                                                || salesJournalBatch.getSalesTenderData().size() > 0
                                                || salesJournalBatch.getSalesTaxData().size() > 0) {
                                            data = sunService.sendJournalData(null, salesJournalBatch,
                                                    syncJobType, account, voucher);
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

                        }
                        else if (addedSalesBatches.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)) {
                            List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);

                            File file = null;
                            String fileStoragePath = "";
                            SalesFileDelimiterExporter exporter = new SalesFileDelimiterExporter(syncJobType, salesList);
                            if (syncJobType.getConfiguration().exportFilePerLocation) {
                                ArrayList<File> files = createSalesFilePerLocation(addedSalesBatches,
                                        syncJobType, account.getName());
                                for (File f : files) {
                                    imageService.storeFile(f);
                                }
                            } else {
                                file = exporter.prepareNDFFile(salesList, syncJobType, account.getName(), "");
                                if(file != null)
                                    fileStoragePath = imageService.storeFile(file);
                            }

                            // Check if the account configured for FTP
                            AccountCredential credential = ftpService.getAccountCredential(account);

                            if(credential.getHost().equals("") || credential.getPassword().equals("") ){
                                syncJobDataService.updateSyncJobDataStatus(salesList, Constants.SUCCESS);
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Get sales successfully.", Constants.SUCCESS);

                                response.setStatus(true);
                                response.setMessage("Get sales successfully.");
                                return response;
                            }

                            if (file != null && !fileStoragePath.equals("") &&
                                    ftpService.sendFile(credential, fileStoragePath, file.getName())) {

                                syncJobDataService.updateSyncJobDataStatus(salesList, Constants.SUCCESS);
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Sync sales successfully.", Constants.SUCCESS);

                                response.setStatus(true);
                                response.setMessage("Sync sales successfully.");
                            } else {
                                syncJobDataService.updateSyncJobDataStatus(salesList, Constants.FAILED);
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Failed to sync sales to sun system via FTP.", Constants.FAILED);

                                response.setStatus(true);
                                response.setMessage("Failed to sync sales to sun system via FTP.");
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


    public Response syncPOSSalesInDayRange(String userId, Account account) throws ParseException, IOException {
        Response response = new Response();
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);

        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        DateFormat fileDateFormat = new SimpleDateFormat("MMyyy");
        DateFormat monthFormat = new SimpleDateFormat("MM");

        int tryCount = 2;

        /*
         * Sync days of last month
         * */
        if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY_PER_MONTH)) {
            syncJobType.getConfiguration().timePeriod = Constants.USER_DEFINED;

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            int numDays = calendar.getActualMaximum(Calendar.DATE);
            String startDate;

            while (numDays != 0) {
                startDate = dateFormat.format(calendar.getTime());
                syncJobType.getConfiguration().fromDate = startDate;
                syncJobType.getConfiguration().toDate = startDate;
                syncJobTypeRepo.save(syncJobType);

                response = getPOSSales(userId, account);
                if (response.isStatus()) {
                    calendar.add(Calendar.DATE, +1);
                    numDays--;
                }
            }

            /*
             * Generate single file
             * */
            String month = monthFormat.format(calendar.getTime());
            String date = fileDateFormat.format(calendar.getTime());

            String path = account.getName();
            String fileName = date + ".ndf";
            boolean perLocation = syncJobType.getConfiguration().exportFilePerLocation;

            GeneralExporterMethods exporterMethods = new GeneralExporterMethods(fileName);
            exporterMethods.generateSingleFile(null, path, month, fileName, perLocation);

            String message = "Sync sales of last month successfully";
            response.setStatus(true);
            response.setMessage(message);
        }
        /*
         * Sync days in range
         * */
        else if (syncJobType.getConfiguration().timePeriod.equals(Constants.USER_DEFINED)) {
            String startDate = syncJobType.getConfiguration().fromDate;
            String endDate = syncJobType.getConfiguration().toDate;

            Date date = dateFormat.parse(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            while (!startDate.equals(endDate)) {
                response = getPOSSales(userId, account);

                if (response.isStatus() || tryCount == 0) {
                    tryCount = 2;
                    calendar.add(Calendar.DATE, +1);
                    startDate = dateFormat.format(calendar.getTime());
                    syncJobType.getConfiguration().fromDate = startDate;
                    syncJobTypeRepo.save(syncJobType);
                }
                tryCount--;
            }

            String message = "Sync sales successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } else {
            if (syncJobType.getConfiguration().timePeriod.equals(Constants.YESTERDAY)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -1);

                syncJobType.getConfiguration().fromDate = dateFormat.format(calendar.getTime());
                syncJobTypeRepo.save(syncJobType);
            }

            response = getPOSSales(userId, account);
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
                syncJobType.getConfiguration().salesConfiguration.tenders = tenders;
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
                syncJobType.getConfiguration().salesConfiguration.taxes = taxes;
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
                syncJobType.getConfiguration().salesConfiguration.majorGroups = majorGroups;
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

    @RequestMapping("/addDiscount")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addDiscount(@RequestBody ArrayList<Discount> discounts,
                                                @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().salesConfiguration.discounts = discounts;
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setSalesDiscount(discounts);
                response.setMessage("Update sales discount successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales discount.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping("/addServiceCharge")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addServiceCharge(@RequestBody ArrayList<ServiceCharge> serviceCharges,
                                                     @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                     Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().salesConfiguration.serviceCharges = serviceCharges;
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update sales service charge successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales service charge.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @RequestMapping("/addSalesStatistics")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addSalesStatistics(@RequestBody ArrayList<SalesStatistics> statistics,
                                                       @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                       Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().salesConfiguration.statistics = statistics;
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update sales statistics successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales statistics.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private ArrayList<File> createSalesFilePerLocation(List<JournalBatch> salesBatches, SyncJobType syncJobType,
                                                       String AccountName) {
        ArrayList<File> locationFiles = new ArrayList<>();
        try {
            File file;
            List<SyncJobData> salesList;
            SalesFileDelimiterExporter excelExporter;

            for (JournalBatch locationBatch : salesBatches) {
                salesList = new ArrayList<>();
                salesList.addAll(locationBatch.getSalesMajorGroupGrossData());
                salesList.addAll(locationBatch.getSalesTaxData());
                salesList.addAll(locationBatch.getSalesServiceChargeData());
                salesList.addAll(locationBatch.getSalesDiscountData());
                salesList.addAll(locationBatch.getSalesTenderData());
                if (locationBatch.getSalesDifferentData().getId() != null) {
                    salesList.add(locationBatch.getSalesDifferentData());
                }
                if (locationBatch.getStatisticsData().size() > 0) {
                    salesList.addAll(locationBatch.getStatisticsData());
                }

                excelExporter = new SalesFileDelimiterExporter(syncJobType, salesList);
                file = excelExporter.prepareNDFFile(salesList, syncJobType, AccountName, locationBatch.getCostCenter().costCenterReference);
                if(file != null)
                    locationFiles.add(file);
            }
            return locationFiles;
        } catch (Exception e) {
            e.printStackTrace();
            return locationFiles;
        }
    }

}

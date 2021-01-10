package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.excelExporters.SalesExcelExporter;
import com.sun.supplierpoc.fileDelimiterExporters.SalesFileDelimiterExporter;
import com.sun.supplierpoc.ftp.FtpClient;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.systemsunion.security.IAuthenticationVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
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
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private SunService sunService;

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
            response = syncPOSSalesInDayRange(user.getId(), account);
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

    public Response getPOSSales(String userId, Account account) {
        Response response = new Response();
        SyncJob syncJob = null;
        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);

            ArrayList<Discount> discounts = syncJobType.getConfiguration().getDiscounts();
            ArrayList<Tender> tenders = syncJobType.getConfiguration().getTenders();
            ArrayList<Tax> taxes = syncJobType.getConfiguration().getTaxes();
            ArrayList<MajorGroup> majorGroups = syncJobType.getConfiguration().getMajorGroups();
            ArrayList<RevenueCenter> revenueCenters = syncJobType.getConfiguration().getRevenueCenters();
            ArrayList<ServiceCharge> serviceCharges = syncJobType.getConfiguration().getServiceCharges();
            ArrayList<CostCenter> locations = generalSettings.getLocations();

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

//            if (syncJobType.getConfiguration().getRevenue().equals("")) {
//                String message = "Configure revenue before sync sales.";
//                response.setMessage(message);
//                response.setStatus(false);
//                return response;
//            }

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

            if (syncJobType.getConfiguration().getGrossDiscountSales().equals("")) {
                String message = "Configure sales gross/gross less discount before sync sales.";
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

            //////////////////////////////////////// End Validation ////////////////////////////////////////////////////////

            ArrayList<JournalBatch> addedSalesBatches = new ArrayList<>();

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), syncJobType.getId(), 0);

            syncJobRepo.save(syncJob);

            try {
                Response salesResponse = salesService.getSalesData(syncJobType, locations,
                        majorGroups, tenders, taxes, discounts, serviceCharges, revenueCenters, account);

                if (salesResponse.isStatus()) {
                    if (salesResponse.getJournalBatches().size() > 0) {
                        // Save Sales Entries
                        addedSalesBatches = salesService.saveSalesJournalBatchesData(salesResponse, syncJob,
                                syncJobType, account);

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
                        else if (addedSalesBatches.size() > 0 && account.getERD().equals(Constants.EXPORT_TO_SUN_ERD)){
                            ArrayList<AccountCredential> accountCredentials = account.getAccountCredentials();
                            AccountCredential sunCredentials = account.getAccountCredentialByAccount(Constants.SUN, accountCredentials);

                            String username = sunCredentials.getUsername();
                            String password = sunCredentials.getPassword();
                            String host = sunCredentials.getHost();
                            int port = sunCredentials.getPort();
                            List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);

                            if (!username.equals("") && !password.equals("") && !host.equals("")){
                                FtpClient ftpClient = new FtpClient(host, username, password);
                                if(ftpClient.open()){
                                    File file = createSalesFile(salesList, syncJobType);

//                                if (ftpClient.putFileToPath(file, fileName)){
                                    if (true){
                                        syncJobDataService.updateSyncJobDataStatus(salesList, Constants.SUCCESS);
                                        syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                                "Sync sales successfully.", Constants.SUCCESS);

                                        response.setStatus(true);
                                        response.setMessage("Sync sales successfully.");
                                    }else {
                                        syncJobDataService.updateSyncJobDataStatus(salesList, Constants.FAILED);
                                        syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                                "Failed to sync sales to sun system via FTP.", Constants.FAILED);

                                        response.setStatus(true);
                                        response.setMessage("Failed to sync sales to sun system via FTP.");
                                    }
                                    ftpClient.close();
                                }
                                else {
                                    syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                            "Failed to connect to sun system via FTP.", Constants.FAILED);

                                    response.setStatus(false);
                                    response.setMessage("Failed to connect to sun system via FTP.");
                                }
                            }
                            else{
                                File file = createSalesFile(salesList, syncJobType);

                                syncJobDataService.updateSyncJobDataStatus(salesList, Constants.SUCCESS);
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Sync sales successfully.", Constants.SUCCESS);

                                response.setStatus(true);
                                response.setMessage("Sync sales successfully.");
                            }

                        }
                        else {
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


    private Response syncPOSSalesInDayRange(String userId, Account account){
        Response response = new Response();

        String[] datesArray = {"2021-01-03", "2021-01-04", "2021-01-05", "2021-01-06", "2021-01-07",
                "2021-01-08", "2021-01-09"};

        for (String date : datesArray) {
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);
            syncJobType.getConfiguration().setFromDate(date);
            syncJobType.getConfiguration().setToDate(date);
            syncJobTypeRepo.save(syncJobType);

            response = getPOSSales(userId, account);

            if (!response.isStatus()){
                return response;
            }
        }

        String message = "Sync sales from 3-10-2020 to 31-10-2020";
        response.setStatus(true);
        response.setMessage(message);
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
                syncJobType.getConfiguration().setDiscounts(discounts);
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
                syncJobType.getConfiguration().setServiceCharges(serviceCharges);
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

    @RequestMapping("/addRevenueCenter")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addRevenueCenter(@RequestBody ArrayList<RevenueCenter> revenueCenters,
                                                @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().setRevenueCenters(revenueCenters);
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update sales revenue center successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update sales revenue center.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/sales/export/excel")
    public void exportToExcel(@RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Sales" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId,
                false);

        SalesExcelExporter excelExporter = new SalesExcelExporter(salesList);

        excelExporter.export(response);
    }

    @GetMapping("/sales/export/csv")
    public void exportToText(Principal principal,
                             @RequestParam(name = "syncJobId") String syncJobId,
                              HttpServletResponse response) throws IOException {
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        Account account = accountOptional.get();
        response.setContentType("application/octet-stream");

        List<SyncJobData> salesList = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId, false);

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES, account.getId(), false);


        String transactionDate = salesList.get(0).getData().get("transactionDate");
        String fileName = transactionDate.substring(4) + transactionDate.substring(2,4) + transactionDate.substring(0,2);

        String headerKey = HttpHeaders.CONTENT_DISPOSITION;
        String headerValue = "attachment; filename=" + fileName + ".txt";
        response.setHeader(headerKey, headerValue);
        response.setContentType("text/csv");

        SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter(fileName + ".txt", syncJobType, salesList);
        excelExporter.writeSyncData(response.getWriter());
    }

    private File createSalesFile(List<SyncJobData> salesList, SyncJobType syncJobType) {
        try {
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] weekdays = dfs.getWeekdays();

            String transactionDate = salesList.get(0).getData().get("transactionDate");
            Calendar cal = Calendar.getInstance();
            Date date = new SimpleDateFormat("ddMMyyyy").parse(transactionDate);
            cal.setTime(date);
            int day = cal.get(Calendar.DAY_OF_WEEK);

            String dayName = weekdays[day];
            String fileExtension = ".ndf";
            String fileName = dayName.substring(0,3) + transactionDate + fileExtension;

            SalesFileDelimiterExporter excelExporter = new SalesFileDelimiterExporter(
                    fileName, syncJobType, salesList);

            File file = excelExporter.createNDFFile();
            System.out.println(fileName);

            return file;
        }catch (Exception e){
            return new File("Sales.ndf");
        }
    }
}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.fileDelimiterExporters.GeneralExporterMethods;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.*;
import com.sun.supplierpoc.services.restTemplate.SyncSalesWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/salesAPI")
public class SalesAPIController {

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
    private SalesApiService salesApiService;
    @Autowired
    private SalesV2Services salesV2Services;
    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;
    @Autowired
    private GoogleDriveService googleDriveService;
    @Autowired
    private SyncSalesWebService syncSalesWebService;
    @Autowired
    private SendEmailService emailService;

    public Conversions conversions = new Conversions();

    @RequestMapping("/getPOSSales")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> getPOSSalesRequest(Principal principal,
                                                       @RequestParam("endpoint") String endpoint) throws ParseException, IOException {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            response = syncPOSSalesInDayRange(user.getId(), account, endpoint);

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


    public Response syncPOSSalesInDayRange(String userId, Account account, String endpoint) throws ParseException, IOException {
        Response response = new Response();

        SyncJobType syncJobType;

        if(endpoint.equals("Daily")) {
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES_API_Daily, account.getId(), false);
        }else{
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES_API_Monthly, account.getId(), false);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        DateFormat fileDateFormat = new SimpleDateFormat("MMyyy");
        DateFormat monthFormat = new SimpleDateFormat("MM");

        int tryCount = 2;

        if (syncJobType.getConfiguration().timePeriod.equals(Constants.USER_DEFINED)
                && syncJobType.getConfiguration().singleFilePerDay) {
            String startDate = syncJobType.getConfiguration().fromDate;
            String endDate = syncJobType.getConfiguration().toDate;

            Date date = dateFormat.parse(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            while (!startDate.equals(endDate)) {
                startDate = dateFormat.format(calendar.getTime());
                syncJobType.getConfiguration().fromDate = startDate;
                syncJobType.getConfiguration().toDate = startDate;
                syncJobTypeRepo.save(syncJobType);

                response = getPOSSales(userId, account, endpoint);

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
        }

        /*
         * Sync days of last month
         * */
        else if (syncJobType.getConfiguration().schedulerConfiguration.duration.equals(Constants.DAILY_PER_MONTH)) {
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

                response = getPOSSales(userId, account, endpoint);
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

        else {
            if (syncJobType.getConfiguration().timePeriod.equals(Constants.YESTERDAY)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, -1);

                syncJobType.getConfiguration().fromDate = dateFormat.format(calendar.getTime());
                syncJobTypeRepo.save(syncJobType);
            }
            else if (syncJobType.getConfiguration().timePeriod.equals(Constants.LAST_MONTH)) {

                Calendar calendar = Calendar.getInstance();

                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DATE, 1);
                syncJobType.getConfiguration().fromDate = dateFormat.format(calendar.getTime());

                calendar.setTime(new Date());
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                syncJobType.getConfiguration().toDate = dateFormat.format(calendar.getTime());

                syncJobTypeRepo.save(syncJobType);
            }

            response = getPOSSales(userId, account, endpoint);
        }
        return response;
    }

    private Response getPOSSales(String userId, Account account, String endpoint) {
        Response response = new Response();
        SyncJob syncJob = null;
        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType syncJobType;

            if(endpoint.equals("Daily")) {
                syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES_API_Daily, account.getId(), false);
            }else{
                syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.SALES_API_Monthly, account.getId(), false);
            }

            SalesAPIConfig configuration = syncJobType.getConfiguration().salesAPIConfig;
            ArrayList<SalesAPIStatistics> statistics = configuration.statistics;
            List<OrderTypeChannels> orderTypeChannels = configuration.orderTypeChannels;
            ArrayList<CostCenter> locations = generalSettings.getLocations();

            String timePeriod = syncJobType.getConfiguration().timePeriod;
            String fromDate = syncJobType.getConfiguration().fromDate;
            String toDate = syncJobType.getConfiguration().toDate;

            //////////////////////////////////////// Validation ///////////////////////////////////////////////////////////

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

            if(orderTypeChannels == null || orderTypeChannels.size() == 0) {
                response.setMessage("Order Type Channels Not Configured.");
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
                List<OrderTypeChannels> newList = new ArrayList<>(orderTypeChannels);

                salesResponse = salesApiService.getSalesData(syncJobType, locations, statistics, account, newList, endpoint);

                if (salesResponse.isStatus()) {
                    if (salesResponse.getJournalBatches().size() > 0) {

                        // Save Sales Entries
                        addedSalesBatches = salesApiService.saveSalesJournalBatchesData(salesResponse, syncJob,
                                syncJobType.getConfiguration(), account);

                        ArrayList<JournalBatch> journalBatches = salesResponse.getJournalBatches();

                        List<HashMap<String , String>> responseData = new ArrayList<>();
                        for(JournalBatch journalBatch : journalBatches) {
                            if(configuration.apiEndpoint.equals("dailysales")) {
                                response = syncSalesWebService.syncSalesDailyAPI(journalBatch.getSalesAPIStatistics(), configuration, responseData);
                            }else{
                                response = syncSalesWebService.syncSalesMonthlyAPI(journalBatch.getSalesAPIStatistics(), configuration, responseData);
                            }

                            /* Update sync job data */
                            if (response.isStatus()) {
                                syncJobDataService.updateSyncJobDataStatus(journalBatch.getStatisticsData(), Constants.SUCCESS);
                            }else{
                                syncJobDataService.updateSyncJobDataStatus(journalBatch.getStatisticsData(), Constants.FAILED);
                            }
                        }
                        emailService.sendEmaarMail("lyoussef@entrepreware.com", responseData, account, syncJobType, response);
                        emailService.sendEmaarMail("mbendary@entrepreware.com", responseData, account, syncJobType, response);

                        if (response.isStatus()) {
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Get sales successfully.", Constants.SUCCESS);

                                response.setStatus(true);
                                response.setMessage("Get sales successfully.");
                                return response;
                            } else {
                                syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                                        "Failed to send files via Emaar API.", Constants.FAILED);

                                response.setStatus(true);
                                response.setMessage("Failed to send files via Emaar API.");
                                return response;
                            }

                        } else {
                            syncJobService.saveSyncJobStatus(syncJob, 0,
                                    "No sales to add in middleware.", Constants.SUCCESS);

                            response.setStatus(true);
                            response.setMessage("No new sales to add in middleware.");
                        }

                } else {
                    syncJobService.saveSyncJobStatus(syncJob, addedSalesBatches.size(),
                            salesResponse.getMessage(), Constants.FAILED);

                    response.setStatus(false);
                    response.setMessage(salesResponse.getMessage());

                    emailService.sendEmaarMail("lyoussef@entrepreware.com", new ArrayList<>(), account, syncJobType, response);
                    emailService.sendEmaarMail("mbendary@entrepreware.com", new ArrayList<>(), account, syncJobType, response);
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

    @RequestMapping("/addOrderTypeChannel")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Response> addOrderTypeChannel(@RequestBody ArrayList<OrderTypeChannels> orderTypeChannels,
                                                        @RequestParam(name = "syncJobTypeId") String syncJobTypeId,
                                                        Principal principal) {
        Response response = new Response();
        User user = (User) ((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            SyncJobType syncJobType = syncJobTypeRepo.findByIdAndDeleted(syncJobTypeId, false);
            if (syncJobType != null) {
                syncJobType.getConfiguration().salesAPIConfig.orderTypeChannels = orderTypeChannels;
                syncJobTypeRepo.save(syncJobType);

                response.setStatus(true);
                response.setMessage("Update Order Type Channel successfully.");
            } else {
                response.setStatus(false);
                response.setMessage("Failed to update Order Type Channel.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}

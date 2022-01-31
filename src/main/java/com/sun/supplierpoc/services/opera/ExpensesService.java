package com.sun.supplierpoc.services.opera;

import com.google.gson.Gson;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.components.CancelBookingExcelHelper;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.components.ExpensesXMLHelper;
import com.sun.supplierpoc.controllers.opera.MinistryOfTourismResponse;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ExpensesService {
    @Autowired
    SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    ExcelHelper excelHelper;

    @Autowired
    ExpensesXMLHelper expensesXMLHelper;

    @Autowired
    private SyncJobService syncJobService;

    @Autowired
    private SyncJobDataService syncJobDataService;

    @Autowired
    BookingService bookingService;

    @Autowired
    private DBProcessor dbProcessor;

    Conversions conversions = new Conversions();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response fetchExpensesDetailsFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();
        Response expenseResponse;

        SyncJob syncJob;
        BookingConfiguration bookingConfiguration;
        SyncJobType expensesDetailsSyncType;
        SyncJobType bookingSyncType;
        GeneralSettings generalSettings;

        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            bookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
            expensesDetailsSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.EXPENSES_DETAILS_REPORT, account.getId(), false);
            bookingConfiguration = expensesDetailsSyncType.getConfiguration().bookingConfiguration;

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), expensesDetailsSyncType.getId(), 0);
            syncJobRepo.save(syncJob);

        } catch (Exception e) {
            message = "Failed to establish a connection with the database.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        try {
            DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = fileDateFormat.format(new Date());

            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;

            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Expenses/" + fileName;
            String localFilePath = account.getName() + "/Expenses/";

            FileInputStream input = bookingService.downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx")){
                syncJobData = excelHelper.getExpensesUpdateFromXLS(syncJob, input, generalSettings, bookingConfiguration);
            } else if(bookingConfiguration.fileExtension.equals("xml")) {
                syncJobData = expensesXMLHelper.getExpensesUpdateFromDB(syncJob, expensesDetailsSyncType,
                        bookingSyncType, localFilePath + fileName, generalSettings, bookingConfiguration);
            }

            for (SyncJobData syncData : syncJobData) {
                if(syncData.getData().get("roomNo").equals(-1)){
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.FAILED, "Neglected Reservation");
                }else{
                    expenseResponse = sendExpensesDetailsUpdates(syncData, bookingConfiguration);

                    if(expenseResponse.isStatus()){
                        syncJobDataService.updateSyncJobDataStatus(syncData, Constants.SUCCESS, "");
                    }else {
                        syncJobDataService.updateSyncJobDataStatus(syncData, Constants.FAILED, expenseResponse.getMessage());
                    }
                }
            }

            syncJobService.saveSyncJobStatus(syncJob, syncJobData.size(), response.getMessage(), Constants.SUCCESS);

            message = "Sync expenses details successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJobService.saveSyncJobStatus(syncJob, 0, response.getMessage(), Constants.FAILED);

            message = "Failed to sync expenses details.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private Response sendExpensesDetailsUpdates(SyncJobData syncJobData, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();
        try {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(bookingConfiguration.getUsername(), bookingConfiguration.getPassword());

            HashMap<String, Object> data = syncJobData.getData();

            JSONObject json = new JSONObject();
            json.put("transactionId", data.get("transactionId"));
            json.put("channel", bookingConfiguration.getChannel());
            json.put("expenseItems", data.get("items"));

            String body = json.toString();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, body);

            Request request = new Request.Builder()
                    .url(bookingConfiguration.getUrl())
                    .post(requestBody)
                    .addHeader("X-Gateway-APIKey", bookingConfiguration.getGatewayKey())
                    .addHeader("content-type", "application/json")
                    .addHeader("Authorization", credential)
                    .build();

            okhttp3.Response expensesDetailsResponse = client.newCall(request).execute();
            if (expensesDetailsResponse.code() == 200){
                Gson gson = new Gson();
                MinistryOfTourismResponse entity = gson.fromJson(expensesDetailsResponse.body().string(), MinistryOfTourismResponse.class);

                if(entity.getErrorCode().contains("0")){
                    message = "Expenses Details send successfully.";
                    response.setStatus(true);
                    response.setMessage(message);
                }else{
                    /* Parse Error */
                    message = parseExpensesErrorMessage(entity.getErrorCode());
                    response.setStatus(false);
                    response.setMessage(message);
                }
            }else {
                message = expensesDetailsResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            response.setStatus(false);
            response.setMessage(message);
        }

        return response;
    }

    private String parseExpensesErrorMessage(List<String> errorCodes){
        String message = "";
        String code = errorCodes.get(0);
        switch (code) {
            case "1":
                message = "Invalid Transaction ID or this Transaction ID not found in MT database.";
                break;
            case "2":
                message = "Invalid Expense Date. It must be numeric in YYYYMMDD format Date must be Gregorian Only.";
                break;
            case "3":
                message = "Invalid Item Number.";
                break;
            case "4":
                message = "ItemNumber not found in MT Database.";
                break;
            case "5":
                message = "Invalid Expense Type ID.";
                break;
            case "6":
                message = "Invalid Unit Price. It must be numeric.";
                break;
            case "7":
                message = "Invalid Discount. It must be numeric only If provided.";
                break;
            case "8":
                message = "Invalid VAT. It must be numeric in Amount only. It can contain 0.";
                break;
            case "9":
                message = "Invalid Municipality Tax. It must be numeric in Amount only. It can contain 0.";
                break;
            case "10":
                message = "Invalid Grand Total. It must be numeric in Amount only.";
                break;

            case "12":
                message = "Invalid Payment Type value.";
                break;
            case "13":
                message = "No checkout data found for TransactionID. Please call this api once the checkout is done";
                break;
            case "14":
                message = "Invalid CU Flag Value. It must be 1=Add, 2=Update.";
                break;
            case "15":
                message = "Same Transaction ID already found with Item Number. Please send it with CUFlag =2 if you wish to update.";
                break;

            case "100":
                message = "Invalid Credentials. Authentication failed.";
                break;
            case "101":
                message = "Internal Server Error. Please try again later.";
                break;
        }

        return message;
    }

}

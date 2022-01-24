package com.sun.supplierpoc.services.opera;

import com.google.gson.Gson;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.*;
import com.sun.supplierpoc.controllers.opera.MinistryOfTourismResponse;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    SyncJobDataRepo syncJobDataRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    ExcelHelper excelHelper;

    @Autowired
    ExpensesXMLHelper expensesXMLHelper;

    @Autowired
    NewBookingExcelHelper bookingExcelHelper;

    @Autowired
    CancelBookingExcelHelper cancelBookingExcelHelper;

    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response fetchNewBookingFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();
        Response createBookingResponse;

        SyncJob syncJob;
        SyncJobType syncJobType;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;

        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
            bookingConfiguration = syncJobType.getConfiguration().bookingConfiguration;

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), syncJobType.getId(), 0);
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
//            fileName = "Booking20211212.xml";
            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Booking/" + fileName;
            String localFilePath = account.getName() + "/Booking/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.toLowerCase().equals("xlsx"))
                syncJobData = bookingExcelHelper.getNewBookingFromExcel(syncJob, generalSettings, syncJobType, input);
            else if(bookingConfiguration.fileExtension.toLowerCase().equals("xml"))
                syncJobData = bookingExcelHelper.getNewBookingFromXML(syncJob, generalSettings, syncJobType, localFilePath + fileName);


            for (SyncJobData syncData : syncJobData) {
                createBookingResponse = sendNewBooking(syncData, bookingConfiguration);
                if(createBookingResponse.isStatus()){
                    HashMap<String, Object> data = syncData.getData();
                    data.put("transactionId", createBookingResponse.getMessage());
                    syncData.setData(data);
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.SUCCESS, "");
                }else {
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.FAILED, createBookingResponse.getMessage());
                }
            }
            syncJobService.saveSyncJobStatus(syncJob, syncJobData.size(), "Sync new booking successfully.", Constants.SUCCESS);

            response.setStatus(true);
            response.setMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
            syncJobService.saveSyncJobStatus(syncJob, 0, "Failed to sync new booking.", Constants.FAILED);
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private Response sendNewBooking(SyncJobData syncJobData, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();
        try {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(bookingConfiguration.getUsername(), bookingConfiguration.getPassword());

            HashMap<String, Object> data = syncJobData.getData();

            JSONObject json = new JSONObject();
            json.put("bookingNo", String.valueOf(data.get("bookingNo")));
            if(!String.valueOf(data.get("transactionId")).equals(""))
                json.put("transactionId", String.valueOf(data.get("transactionId")));
            json.put("nationalityCode", String.valueOf(data.get("nationalityCode")));
            json.put("checkInDate", String.valueOf(data.get("checkInDate")));
            json.put("checkOutDate", String.valueOf(data.get("checkOutDate")));
            json.put("totalDurationDays", String.valueOf(data.get("totalDurationDays")));
            json.put("allotedRoomNo", String.valueOf(data.get("allotedRoomNo")));
            json.put("roomRentType", String.valueOf(data.get("roomRentType")));
            json.put("dailyRoomRate", String.valueOf(data.get("dailyRoomRate")));
            json.put("totalRoomRate", String.valueOf(data.get("totalRoomRate")));
            json.put("vat", String.valueOf(data.get("vat")));
            json.put("municipalityTax", String.valueOf(data.get("municipalityTax")));
            json.put("discount", String.valueOf(data.get("discount")));
            json.put("grandTotal", String.valueOf(data.get("grandTotal")));
            json.put("transactionTypeId", String.valueOf(data.get("transactionTypeId")));
            json.put("gender", String.valueOf(data.get("gender")));
            json.put("checkInTime", String.valueOf(data.get("checkInTime")));
            json.put("checkOutTime", String.valueOf(data.get("checkOutTime")));
            json.put("customerType", String.valueOf(data.get("customerType")));
            json.put("noOfGuest", String.valueOf(data.get("noOfGuest")));
            json.put("roomType", String.valueOf(data.get("roomType")));
            json.put("purposeOfVisit", String.valueOf(data.get("purposeOfVisit")));
            if(!String.valueOf(data.get("dateOfBirth")).equals(""))
                json.put("dateOfBirth", String.valueOf(data.get("dateOfBirth")));
            json.put("paymentType", String.valueOf(data.get("paymentType")));
            json.put("noOfRooms", String.valueOf(data.get("noOfRooms")));
            json.put("cuFlag", String.valueOf(data.get("cuFlag")));
            json.put("channel", bookingConfiguration.getChannel());

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

            okhttp3.Response bookingResponse = client.newCall(request).execute();
            if (bookingResponse.code() == 200){
                Gson gson = new Gson();
                MinistryOfTourismResponse entity = gson.fromJson(bookingResponse.body().string(), MinistryOfTourismResponse.class);

                if(entity.getErrorCode().contains("0")){
                    response.setStatus(true);
                    response.setMessage(entity.getTransactionId());
                }else{
                    /* Parse Error */
                    message = parseBookingErrorMessage(entity.getErrorCode());
                    response.setStatus(false);
                    response.setMessage(message);
                }
            }else {
                message = bookingResponse.message();
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

    private String parseBookingErrorMessage(List<String> errorCodes){
        String message = "";
        String code = errorCodes.get(0);
        switch (code) {
            case "1":
                message = "Booking No is required and can contains Arabic, English," +
                        " and Numeric & Alphanumeric.Booking No is required and can contains Arabic, English, and Numeric & Alphanumeric.";
                break;
            case "2":
                message = "Invalid Nationality Code. It must be numeric and available in lookup list.";
                break;
            case "3":
                message = "Invalid Check In Date.";
                break;
            case "4":
                message = "Invalid Check Out Date.";
                break;
            case "5":
                message = "Invalid Total Duration Days.";
                break;
            case "6":
                message = "Invalid Allotted Room No.";
                break;
            case "7":
                message = "Invalid Room Rent Type.";
                break;
            case "8":
                message = "Invalid Daily Room Rate.";
                break;
            case "9":
                message = "Invalid Total Room Rate.";
                break;
            case "10":
                message = "Invalid VAT Value.";
                break;
            case "11":
                message = "Invalid Municipality Tax.";
                break;
            case "12":
                message = "Invalid Discount. It must be numeric & Amount only.";
                break;
            case "13":
            case "14":
                message = "Invalid Grand Total.";
                break;
            case "15":
                message = "Invalid Transaction Type Id.";
                break;
            case "16":
                message = "Invalid Gender.";
                break;
            case "17":
                message = "Invalid User Id or UserId not found.";
                break;
            case "18":
                message = "Invalid Transaction No or this Transaction No not found in MT database.";
                break;
            case "19":
                message = "Invalid Check In Time.";
                break;
            case "20":
                message = "Invalid Check Out Time.";
                break;
            case "21":
                message = "Invalid Customer Type.";
                break;
            case "22":
                message = "Invalid No Of Guest. It must be numeric.";
                break;
            case "23":
                message = "Invalid Room Type.";
                break;
            case "24":
                message = "Invalid Purpose of Visit.";
                break;
            case "25":
                message = "Invalid Payment Type value.";
                break;
            case "26":
                message = "Invalid number of rooms";
                break;
            case "27":
                message = "Invalid Create or Update Flag. The value must be 1 or 2.";
                break;
            case "28":
                message = "Invalid Date Of Birth.";
                break;
            case "29":
                message = "This TransactionID & TransactionTypeID already exist.";
                break;
            case "30":
                message = "Updates on Booking & Check In are not allowed because Check out is created.";
                break;
            case "31":
                message = "Updates on Booking are not allowed because Check-In is created for same booking.";
                break;
            case "32":
                message = "No updates allowed on this TransactionID because it is already cancelled.";
                break;
            case "33":
                message = "You are trying to update a record, which does not exist.";
                break;
            case "34":
                message = "Booking number and User ID combination should be unique.";
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

    public Response fetchCancelBookingFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();
        Response cancelBookingResponse;

        SyncJob syncJob;
        SyncJobType syncJobType;
        SyncJobType newBookingSyncType;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;
        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);
            newBookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
            bookingConfiguration = syncJobType.getConfiguration().bookingConfiguration;


            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), syncJobType.getId(), 0);
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
            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/CancelBooking/" + fileName;
            String localFilePath = account.getName() + "/CancelBooking/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx"))
                syncJobData = cancelBookingExcelHelper.getCancelBookingFromExcel(syncJob, generalSettings,
                        syncJobType, newBookingSyncType, input);
            else if(bookingConfiguration.fileExtension.equals("xml"))
                syncJobData = cancelBookingExcelHelper.getCancelBookingFromXML(syncJob, generalSettings,
                        syncJobType, newBookingSyncType, localFilePath + fileName);

            for (SyncJobData syncData : syncJobData) {
                cancelBookingResponse = sendCancelBooking(syncData, bookingConfiguration);

                if(cancelBookingResponse.isStatus()){
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.SUCCESS, "");
                }else {
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.FAILED, cancelBookingResponse.getMessage());
                }
            }
            syncJobService.saveSyncJobStatus(syncJob, syncJobData.size(), "Sync cancel booking successfully.", Constants.SUCCESS);

            message = "Sync cancel booking successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync cancel booking.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private Response sendCancelBooking(SyncJobData syncJobData, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();
        try {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(bookingConfiguration.getUsername(), bookingConfiguration.getPassword());

            HashMap<String, Object> data = syncJobData.getData();

            JSONObject json = new JSONObject();
            json.put("transactionId", String.valueOf(data.get("transactionId")));
            json.put("cancelReason", String.valueOf(data.get("cancelReason")));
            json.put("cancelWithCharges", String.valueOf(data.get("cancelWithCharges")));
            json.put("chargeableDays", String.valueOf(data.get("chargeableDays")));
            json.put("roomRentType", String.valueOf(data.get("roomRentType")));
            json.put("dailyRoomRate", String.valueOf(data.get("dailyRoomRate")));
            json.put("totalRoomRate", String.valueOf(data.get("totalRoomRate")));
            json.put("vat", String.valueOf(data.get("vat")));
            json.put("municipalityTax", String.valueOf(data.get("municipalityTax")));
            json.put("discount", String.valueOf(data.get("discount")));
            json.put("grandTotal", String.valueOf(data.get("grandTotal")));
            json.put("paymentType", String.valueOf(data.get("paymentType")));
            json.put("cuFlag", String.valueOf(data.get("cuFlag")));
            json.put("channel", bookingConfiguration.getChannel());

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

            okhttp3.Response bookingResponse = client.newCall(request).execute();
            if (bookingResponse.code() == 200){
                Gson gson = new Gson();
                MinistryOfTourismResponse entity = gson.fromJson(bookingResponse.body().string(), MinistryOfTourismResponse.class);

                if(entity.getErrorCode().contains("0")){
                    message = "Create new booking successfully.";
                    response.setStatus(true);
                    response.setMessage(message);
                }else{
                    /* Parse Error */
                    message = parseCancelBookingErrorMessage(entity.getErrorCode());
                    response.setStatus(false);
                    response.setMessage(message);
                }
            }else {
                message = bookingResponse.message();
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

    private String parseCancelBookingErrorMessage(List<String> errorCodes){
        String message = "";
        String code = errorCodes.get(0);
        switch (code) {
            case "1":
                message = "Invalid Transaction ID or this Transaction ID not found in MT database.";
                break;
            case "2":
                message = "Invalid Cancel Reason.";
                break;
            case "3":
                message = "Invalid Cancel With Charges.";
                break;
            case "4":
                message = "Invalid Chargeable Days.";
                break;
            case "5":
                message = "Invalid Room Rent Type.";
                break;
            case "6":
                message = "Invalid Daily Room Rate.";
                break;
            case "7":
                message = "Invalid Total Room Rate.";
                break;
            case "8":
                message = "Invalid VAT.";
                break;
            case "9":
                message = "Invalid Municipality Tax.";
                break;
            case "10":
                message = "Invalid Discount.";
                break;
            case "11":
                message = "Invalid Grand Total.";
                break;
            case "12":
                message = "Invalid User Id or UserId not found.";
                break;
            case "13":
                message = "Invalid Payment Type value.";
                break;
            case "14":
                message = "This operation is allowed only before Check In.";
                break;
            case "15":
                message = "Invalid CU Flag. The value must be 1 or 2.";
                break;
            case "16":
                message = "This transaction is already cancelled.";
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
//            fileName = "SingleExpenses" + '.' + bookingConfiguration.fileExtension;
//            fileName = "expensesperreserv2.xml";
            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Expenses/" + fileName;
            String localFilePath = account.getName() + "/Expenses/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

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

    public FileInputStream downloadFile(String fileName, String filePath, String localFilePath) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(filePath).openStream());

        File file = new File(localFilePath + fileName);
        boolean status = file.getParentFile().mkdirs();
        if (status)
            file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(localFilePath + fileName);

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }

        return new FileInputStream(file);
    }
}

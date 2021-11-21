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
    OccupancyXMLHelper occupancyXMLHelper;
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
                    message = "Create new booking successfully.";
                    response.setStatus(true);
                    response.setMessage(message);
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

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

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

    public Response fetchOccupancyFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        SyncJobType syncJobType;
        BookingConfiguration bookingConfiguration;
        try {
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.OCCUPANCY_UPDATE_REPORT, account.getId(), false);
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
            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Occupancy/" + fileName;
            String localFilePath = account.getName() + "/Occupancy/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);
            List<SyncJobData> syncJobData = new ArrayList<>();

            if(bookingConfiguration.fileExtension.equals("xlsx")){
                syncJobData = excelHelper.getOccupancyFromExcel(syncJob, input);
            } else if(bookingConfiguration.fileExtension.equals("xml")) {
                syncJobData = occupancyXMLHelper.getOccupancyFromXML(syncJob, localFilePath + fileName);
            }

            /* Send occupancy update */
            response = sendOccupancyUpdates(syncJobData, bookingConfiguration);

            if(response.isStatus()){
                syncJobService.saveSyncJobStatus(syncJob, syncJobData.size(), response.getMessage(), Constants.SUCCESS);
                syncJobDataService.updateSyncJobDataStatus(syncJobData, Constants.SUCCESS);
            }else {
                syncJobService.saveSyncJobStatus(syncJob, syncJobData.size(), response.getMessage(), Constants.FAILED);
                syncJobDataService.updateSyncJobDataStatus(syncJobData, Constants.FAILED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            syncJobService.saveSyncJobStatus(syncJob, 0, e.getMessage(), Constants.FAILED);
            message = "Failed to sync occupancy Updates.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private Response sendOccupancyUpdates(List<SyncJobData> syncJobData, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();
        try {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(bookingConfiguration.getUsername(), bookingConfiguration.getPassword());

            HashMap<String, Object> data = syncJobData.get(0).getData();

            JSONObject json = new JSONObject();
            json.put("updateDate", String.valueOf(data.get("updateDate")));
            json.put("roomsOccupied", String.valueOf(data.get("roomsOccupied")));
            json.put("roomsAvailable", String.valueOf(data.get("roomsAvailable")));
            json.put("roomsBooked", String.valueOf(data.get("roomsBooked")));
            json.put("roomsOnMaintenance", String.valueOf(data.get("roomsOnMaintenance")));
            json.put("totalRooms", String.valueOf(data.get("totalRooms")));
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

            okhttp3.Response occupancyUpdateResponse = client.newCall(request).execute();
            if (occupancyUpdateResponse.code() == 200){
                Gson gson = new Gson();
                MinistryOfTourismResponse entity = gson.fromJson(occupancyUpdateResponse.body().string(), MinistryOfTourismResponse.class);

                if(entity.getErrorCode().contains("0")){
                    message = "Occupancy update send successfully.";
                    response.setStatus(true);
                    response.setMessage(message);
                }else{
                    /* Parse Error */
                    message = parseOccupancyErrorMessage(entity.getErrorCode());
                    response.setStatus(false);
                    response.setMessage(message);
                }
            }else {
                message = occupancyUpdateResponse.message();
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

    private String parseOccupancyErrorMessage(List<String> errorCodes){
        String message = "";
        String code = errorCodes.get(0);
        switch (code) {
            case "1":
                message = "Invalid Update Date, It should be numeric in following format YYYYMMDD.";
                break;
            case "2":
                message = "Invalid Rooms Occupied. It must be numeric only. It can contain 0.";
                break;
            case "3":
                message = "Invalid Rooms Available. It must be numeric only. It can contain 0.";
                break;
            case "4":
                message = "Invalid Rooms Booked. It must be numeric only. It can contain 0.";
                break;
            case "5":
                message = "Invalid Rooms on Maintenance. It must be numeric only. It can contain 0.";
                break;
            case "6":
                message = "Invalid UserId or User Id not found.";
                break;
            case "7":
                message = "Invalid Transaction Id or TransactionId not found.";
                break;
            case "8":
                message = "Invalid Day Closing value. It should be true or false.";
                break;
            case "9":
                message = "Invalid Total Rooms value. It must be numeric only. It can contain 0.";
                break;
            case "10":
                message = "Incorrect Total Rooms Value.";
                break;
            case "11":
                message = "Invalid Total Adults value.";
                break;
            case "12":
                message = "Invalid Total Children value.";
                break;
            case "13":
                message = "Invalid Total Guests value.";
                break;
            case "14":
                message = "Incorrect Total Guests value.";
                break;
            case "15":
                message = "Total Guests is mandatory if day closing is true.";
                break;
            case "16":
                message = "Invalid Total Revenue value.";
                break;
            case "17":
                message = "Total Revenue is mandatory if day closing is true.";
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

        SyncJob syncJob;
        BookingConfiguration bookingConfiguration;
        SyncJobType expensesDetailsSyncType;
        GeneralSettings generalSettings;

        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
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
            // fileName = "Expenses20210617.xls"
            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
            String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Expenses/" + fileName;
            String localFilePath = account.getName() + "/Expenses/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx")){
                syncJobData = excelHelper.getExpensesUpdateFromXLS(syncJob, input, generalSettings, bookingConfiguration);
            } else if(bookingConfiguration.fileExtension.equals("xml")) {
                syncJobData = expensesXMLHelper.getExpensesUpdateFromXLS(syncJob, localFilePath + fileName,
                        generalSettings, bookingConfiguration);
            }

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

            message = "Sync expenses details successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync expenses details.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private FileInputStream downloadFile(String fileName, String filePath, String localFilePath) throws IOException {
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

package com.sun.supplierpoc.services.opera;

import com.google.gson.Gson;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.components.CancelBookingExcelHelper;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.components.ExpensesXMLHelper;
import com.sun.supplierpoc.components.NewBookingExcelHelper;
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

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CancelBookingService {
    @Autowired
    SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    ExcelHelper excelHelper;

    @Autowired
    CancelBookingExcelHelper cancelBookingExcelHelper;

    @Autowired
    private SyncJobService syncJobService;

    @Autowired
    private SyncJobDataService syncJobDataService;

    @Autowired
    BookingService bookingService;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

            FileInputStream input = bookingService.downloadFile(fileName, filePath, localFilePath);

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

}

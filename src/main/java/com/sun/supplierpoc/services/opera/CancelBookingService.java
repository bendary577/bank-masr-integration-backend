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
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.Package;
import com.sun.supplierpoc.models.opera.booking.RateCode;
import com.sun.supplierpoc.models.opera.booking.ReservationRow;
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
import java.text.ParseException;
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

    @Autowired
    private DBProcessor dbProcessor;

    Conversions conversions = new Conversions();
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SyncJobData createCancelBookingObject(ReservationRow reservationRow, Account account){
        HashMap<String, Object> data = new HashMap<>();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
        SyncJobType cancelSyncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);

        String typeName;
        String tempDate;
        BookingType bookingType;

        double chargeableDays = 0;
        double basicRoomRate = 0;
        double vat = 0;
        double municipalityTax = 0;
        double serviceCharge = 0;

        double totalPackageAmount = 0;
        double totalPackageVat = 0;
        double totalPackageMunicipality = 0;
        double totalPackageServiceCharges = 0;

        double grandTotal = 0;
        int nights = 0;

        RateCode rateCode = new RateCode();
        rateCode.serviceChargeRate = cancelSyncJobType.getConfiguration().bookingConfiguration.serviceChargeRate;
        rateCode.municipalityTaxRate = cancelSyncJobType.getConfiguration().bookingConfiguration.municipalityTaxRate;
        rateCode.vatRate = cancelSyncJobType.getConfiguration().bookingConfiguration.vatRate;
        rateCode.basicPackageValue = 0;

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> cancelReasons = generalSettings.getCancelReasons();

        Date updateDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date checkIn = null;
        Date checkOut = null;

        /* Reservation */
        data.put("bookingNo", reservationRow.bookingNo);

        /* Guest Info - Use Mapping Tables */
        bookingType = conversions.checkBookingTypeExistence(paymentTypes, reservationRow.paymentType);
        data.put("paymentType", bookingType.getTypeId());

        bookingType = conversions.checkBookingTypeExistence(cancelReasons, reservationRow.cancelReason);
        data.put("cancelReason", bookingType.getTypeId());

        try {
            if (!reservationRow.checkInDate.equals("")) {
                try {
                    checkIn = new SimpleDateFormat("dd.MM.yy").parse(reservationRow.checkInDate);
                } catch (ParseException e) { // 03-DEC-20
                    checkIn = new SimpleDateFormat("dd-MMMM-yy").parse(reservationRow.checkInDate);
                }
            }
        } catch (Exception e) {
            checkIn = null;
        }

        if (checkIn != null && checkOut != null) {
            nights = conversions.getNights(checkIn, checkOut);
            data.put("roomRentType", conversions.checkRoomRentType(checkIn, checkOut));
        }

        data.put("totalDurationDays", nights);

        /* Payment Info */
        if(nights == 0)
            nights = 1;

        if(reservationRow.cancelWithCharges == 0){ // NO
            chargeableDays = 0;
            basicRoomRate = 0;
            reservationRow.totalRoomRate = 0;
            reservationRow.discount = 0.0;
            vat = 0;
            municipalityTax = 0;
            grandTotal = 0;
        }else{
            /* Get reservation packages - Query from OPERA DB */
            ArrayList<Package> packages = dbProcessor.getReservationPackage(reservationRow.reservNameId,
                    reservationRow.adults, reservationRow.children, reservationRow.noOfRooms);

            for (Package pkg: packages){
                // Calculate totals
                totalPackageAmount += pkg.price;
                totalPackageServiceCharges += pkg.serviceCharge;
                totalPackageMunicipality += pkg.municipalityTax;
                totalPackageVat += pkg.vat;
            }

            basicRoomRate = conversions.roundUpDouble((reservationRow.totalRoomRate + totalPackageAmount)/(nights-1));

            serviceCharge = conversions.roundUpDouble((reservationRow.totalRoomRate * rateCode.serviceChargeRate) / 100);
            municipalityTax = conversions.roundUpDouble((reservationRow.totalRoomRate * rateCode.municipalityTaxRate) / 100);
            vat = conversions.roundUpDouble(((serviceCharge + reservationRow.totalRoomRate) * rateCode.vatRate) / 100);
//        vat = ((municipalityTax + reservationRow.totalRoomRate) * rateCode.vatRate) / 100;

            serviceCharge = conversions.roundUpDouble(serviceCharge + totalPackageServiceCharges);
            municipalityTax = conversions.roundUpDouble(municipalityTax + totalPackageMunicipality);
            vat = conversions.roundUpDouble(vat + totalPackageVat);
            reservationRow.totalRoomRate = conversions.roundUpDouble(reservationRow.totalRoomRate + totalPackageAmount);

            grandTotal = conversions.roundUpDouble((reservationRow.totalRoomRate + vat + municipalityTax)
                    - reservationRow.discount);
        }

        data.put("cancelWithCharges", reservationRow.cancelWithCharges);
        data.put("chargeableDays", chargeableDays);
        data.put("dailyRoomRate", basicRoomRate);
        data.put("totalRoomRate", reservationRow.totalRoomRate);
        data.put("discount", reservationRow.discount);
        data.put("vat", vat);
        data.put("municipalityTax", municipalityTax);
        data.put("grandTotal", grandTotal);

        data.put("transactionId", "");
        data.put("cuFlag", 1); // NEW

        // check if there is booking before cancel it
        ArrayList<SyncJobData> list = syncJobDataService.getDataByBookingNoAndSyncType(reservationRow.bookingNo,
                syncJobType.getId());
        if (list.size() > 0 && !list.get(0).getData().get("transactionId").equals("")){
            data.put("transactionId", (String) list.get(0).getData().get("transactionId"));

            boolean found = false;
            list = syncJobDataService.getDataByBookingNoAndSyncType(reservationRow.bookingNo,
                    cancelSyncJobType.getId());
            // law fi cancel request abl kada w success
            if (list.size() > 0){
                data.put("cuFlag", 2); // Update cancel booking
            }
        }

        SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(), "", cancelSyncJobType.getId());
        return syncJobData;
    }

    public Response fetchCancelBookingFromReport(String userId, Account account, SyncJobData syncJobData) {
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
            List<SyncJobData> syncJobDataList = new ArrayList<>();
            if(syncJobData != null){
                syncJobData.setSyncJobId(syncJob.getId());
                syncJobDataList.add(syncJobData);
            }else{
                DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
                String currentDate = fileDateFormat.format(new Date());

                String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
                String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/CancelBooking/" + fileName;
                String localFilePath = account.getName() + "/CancelBooking/";

                FileInputStream input = bookingService.downloadFile(fileName, filePath, localFilePath);

                if(bookingConfiguration.fileExtension.equals("xlsx"))
                    syncJobDataList = cancelBookingExcelHelper.getCancelBookingFromExcel(syncJob, generalSettings,
                            syncJobType, newBookingSyncType, input);
                else if(bookingConfiguration.fileExtension.equals("xml"))
                    syncJobDataList = cancelBookingExcelHelper.getCancelBookingFromXML(syncJob, generalSettings,
                            syncJobType, newBookingSyncType, localFilePath + fileName);
            }

            for (SyncJobData syncData : syncJobDataList) {
                cancelBookingResponse = sendCancelBooking(syncData, bookingConfiguration);

                if(cancelBookingResponse.isStatus()){
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.SUCCESS, "");
                }else {
                    syncJobDataService.updateSyncJobDataStatus(syncData, Constants.FAILED, cancelBookingResponse.getMessage());
                }
            }
            syncJobService.saveSyncJobStatus(syncJob, syncJobDataList.size(), "Sync cancel booking successfully.", Constants.SUCCESS);

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

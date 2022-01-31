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
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.ExpenseItem;
import com.sun.supplierpoc.models.opera.booking.ExpenseObject;
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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public List<SyncJobData> fetchExpensesDetailsFromDB(String bookingNo, String userId, Account account) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

        try {
            double unitPrice, vat, municipalityTax, grandTotal;

            Response expenseResponse;
            ExpenseObject expenseObject = new ExpenseObject();
            HashMap<String, Object> expenseObjectData;

            ExpenseItem expenseItem;
            ExpenseItem expenseItemTax;

            String[] generatesArray;
            ArrayList<SyncJobData> dataList = new ArrayList<>();
            List<ExpenseItem> expensesItems = new ArrayList<>();
            List<Map<String, Object>> rows;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType bookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
            SyncJobType expensesDetailsSyncType =
                    syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.EXPENSES_DETAILS_REPORT, account.getId(), false);
            BookingConfiguration bookingConfiguration = expensesDetailsSyncType.getConfiguration().bookingConfiguration;

            SyncJob syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), expensesDetailsSyncType.getId(), 0);
            syncJob = syncJobRepo.save(syncJob);

            ArrayList<String> neglectedGroupCodes = bookingConfiguration.neglectedGroupCodes;
            ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
            ArrayList<BookingType> expenseTypes = generalSettings.getExpenseTypes();

            // Fetch all reservation expenses from DB
            rows = dbProcessor.getReservationExpenses(bookingNo);

            // Map rows
            ExpenseItem item;
            String generates = "";
            for (int i = 0; i < rows.size(); i++) {
                item = new ExpenseItem();

                item.bookingNo = (String) rows.get(i).get("booking_no");
                item.paymentType = conversions.checkBookingTypeExistence(paymentTypes,
                        (String) rows.get(i).get("payment_method")).getTypeId();

                try{
                    item.roomNo = Integer.parseInt((String) rows.get(i).get("room"));
                } catch (NumberFormatException e) {
                    if(!rows.get(i).get("room").equals(""))
                        item.roomNo = -1;
                }
                item.unitPrice = ((BigDecimal)rows.get(i).get("net_amount")).toString();
                item.itemNumber = (String) rows.get(i).get("trx_code");
                item.description = (String) rows.get(i).get("description");
                item.expenseTypeId = conversions.checkBookingTypeExistence(expenseTypes, item.itemNumber).getTypeId();

                generates = (String) rows.get(i).get("tax_elements");
                if(generates != null)
                    item.generates = generates.replaceAll(",", "").replaceAll("\\s", "");

                Date updateDate =  (Date) rows.get(i).get("trx_date");
                item.expenseDate = dateFormat.format(updateDate);

                item.cuFlag = "1"; // NEW ITEM

                expensesItems.add(item);
            }

            if(expensesItems.size() == 0)
                return syncJobDataList;

            dataList = syncJobDataService.getSyncJobDataByBookingNoAndType(bookingNo,
                    bookingSyncType.getId());

            expenseObject = new ExpenseObject();

            expenseObject.roomNo = expensesItems.get(0).roomNo;
            expenseObject.bookingNo = bookingNo;
            expenseObject.channel = expensesDetailsSyncType.getConfiguration().bookingConfiguration.getChannel();

            if (dataList.size() > 0) {
                expenseObject.transactionId = (String) dataList.get(0).getData().get("transactionId");

                /* Check Existence */
                dataList = syncJobDataService.getSyncJobDataByBookingNoAndType(bookingNo, expensesDetailsSyncType.getId());
            } else {
                expenseObject.transactionId = "";
                dataList = new ArrayList<>();
            }

            for (int i = 0; i < expensesItems.size(); i++) {
                vat = 0; municipalityTax = 0; grandTotal = 0;
                // Read Expenses row
                expenseItem = expensesItems.get(i);

                // Loop over taxes
                if(!expenseItem.generates.equals("")){
                    generatesArray = expenseItem.generates.split("%");
                }else{
                    generatesArray = new String[0];
                }

                // Skip neglected group code
                if(neglectedGroupCodes.contains(expenseItem.itemNumber)){
                    i += generatesArray.length;
                    continue;
                }

                for (int g = 0; g < generatesArray.length; g++) {
                    i++;
                    expenseItemTax = expensesItems.get(i);

                    if(expenseItemTax.description.toLowerCase().contains("municipality") ||
                            expenseItemTax.description.toLowerCase().contains("muncipality")){
                        expenseItem.municipalityTax = expenseItemTax.unitPrice;
                    }else if(expenseItemTax.description.toLowerCase().contains("vat")){
                        expenseItem.vat = expenseItemTax.unitPrice;
                    }
                }

                // Calculate Grand Total
                unitPrice = Double.parseDouble(expenseItem.unitPrice);
                if(!expenseItem.vat.equals(""))
                    vat = Double.parseDouble(expenseItem.vat);
                if(!expenseItem.municipalityTax.equals(""))
                    municipalityTax = Double.parseDouble(expenseItem.municipalityTax);

                grandTotal = unitPrice + vat + municipalityTax;
                expenseItem.grandTotal = String.valueOf(conversions.roundUpDouble(grandTotal));

                boolean addItem = true;
                for (ExpenseItem ObjItem : expenseObject.items) {
                    if(ObjItem.itemNumber.equals(expenseItem.itemNumber)
                            && !ObjItem.expenseDate.equals(expenseItem.expenseDate)){
                        ObjItem.vat = String.valueOf(vat + Double.parseDouble(ObjItem.vat));
                        ObjItem.municipalityTax = String.valueOf(municipalityTax + Double.parseDouble(ObjItem.municipalityTax));
                        ObjItem.unitPrice = String.valueOf(unitPrice + Double.parseDouble(ObjItem.unitPrice));
                        ObjItem.grandTotal = String.valueOf(grandTotal + Double.parseDouble(ObjItem.grandTotal));
                        addItem = false;
                        break;
                    }
                }

                if(addItem)
                    expenseObject.items.add(expenseItem);
            }

            expensesXMLHelper.saveExpenseObject(syncJob, expensesDetailsSyncType, expenseObject, syncJobDataList);

            for (SyncJobData syncData : syncJobDataList) {
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

            syncJobService.saveSyncJobStatus(syncJob, syncJobDataList.size(), "", Constants.SUCCESS);

        }catch (Exception e){
            e.printStackTrace();
        }
        return syncJobDataList;
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

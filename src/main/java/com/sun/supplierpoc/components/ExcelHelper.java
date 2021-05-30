package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.models.opera.booking.*;
import com.sun.supplierpoc.models.opera.Reservation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import com.sun.supplierpoc.services.SyncJobDataService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcelHelper {
    @Autowired
    SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static List<SyncJobData> getReservationFromExcel(SyncJob syncJob, InputStream is) {

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<SyncJobData> syncJobDataList = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

                Reservation reservation = new Reservation();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0:
                            reservation.setRoom((int) currentCell.getNumericCellValue());
                            break;
                        case 1:
                            reservation.setGuestId((int) currentCell.getNumericCellValue());
                            break;
                        case 2:
                            reservation.setName(currentCell.getStringCellValue());
                            break;
                        case 3:
                            reservation.setPersonsNumber((int) currentCell.getNumericCellValue());
                            break;
                        case 4:
                            reservation.setChildrenNumber((int) currentCell.getNumericCellValue());
                            break;
                        case 5:
                            reservation.setFromDate(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 6:
                            reservation.setToDate(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 7:
                            reservation.setWebsite(String.valueOf(currentCell.getStringCellValue()));
                            break;
                        case 8:
                            reservation.setEmail(String.valueOf(currentCell.getStringCellValue()));
                            break;
                        case 9:
                            try {
                                reservation.setPhone(String.valueOf(currentCell.getStringCellValue()));
                            } catch (Exception e) {
                                reservation.setPhone(String.valueOf(currentCell.getNumericCellValue()));
                            }
                            break;
                        case 10:
                            reservation.setNationality(currentCell.getStringCellValue());
                            break;
                        case 11:
                            reservation.setLastRoom(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 12:
                            reservation.setLastVist(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 13:
                            reservation.setArr(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        case 14:
                            reservation.setNts(String.valueOf(currentCell.getNumericCellValue()));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                SyncJobData syncJobData = new SyncJobData(reservation, "success", "", new Date(), syncJob.getId(), false);
                syncJobDataList.add(syncJobData);
            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public List<SyncJobData> getCancelBookingFromExcel(SyncJob syncJob, GeneralSettings generalSettings,
                                                       SyncJobType syncJobType, SyncJobType newBookingSyncType,
                                                       InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
//        ArrayList<RateCode> rateCodes = generalSettings.getRateCodes();
        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> cancelReasons = generalSettings.getCancelReasons();

        String typeName;
        String status = "";
        String reason = "";

        float roomRate = 0;
        float totalRoomRate = 0;
        float vat = 0;
        float municipalityTax = 0;
        float discount = 0;
        int discountPercent = 0;

        float serviceCharge = 0;
        float grandTotal = 0;
        int nights = 0;
        int noOfRooms = 0;

        RateCode rateCode = new RateCode();
        rateCode.serviceChargeRate = syncJobType.getConfiguration().bookingConfiguration.serviceChargeRate;
        rateCode.municipalityTaxRate = syncJobType.getConfiguration().bookingConfiguration.municipalityTaxRate;
        rateCode.vatRate = syncJobType.getConfiguration().bookingConfiguration.vatRate;
        rateCode.basicPackageValue = 0;

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;
            CancelBookingDetails bookingDetails;

            float paymentAmount = 0;
            float transactionAmount = 0;
            String paymentTypeName;
            BookingType paymentType;

            String cancelReasonName;
            BookingType cancelReason;

            Date arrivalDate = null;
            Date departureDate = null;

            ArrayList<String> columnsName = new ArrayList<>();

            while (rows.hasNext()) {
                status = "";
                reason = "";

                currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    cellsInRow = currentRow.iterator();
                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        columnsName.add(currentCell.getStringCellValue().trim());
                    }
                    rowNumber++;
                    continue;
                }

                cellsInRow = currentRow.iterator();
                bookingDetails = new CancelBookingDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        bookingDetails.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Cancel Reason")) {
                        cancelReasonName = (currentCell.getStringCellValue());
                        cancelReason = conversions.checkBookingTypeExistence(cancelReasons, cancelReasonName);

                        bookingDetails.cancelReason = cancelReason.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Chargeable Days")) {
                        nights = (int) (currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Daily Rate")) {
                        roomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Discount Amount")) {
                        discount = (float) (currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Discount Percent")) {
                        discountPercent = (int) (currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Pay Method")) {
                        paymentTypeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkBookingTypeExistence(paymentTypes, paymentTypeName);

                        bookingDetails.paymentType = paymentType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Pay Amount")) {
                        paymentAmount = (float) (currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Transaction Amount")) {
                        transactionAmount = (float) (currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Arrival Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                arrivalDate  = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                            }
                        } catch (Exception e) {
                            arrivalDate = currentCell.getDateCellValue();
                        }
                    } else if (cellIdx == columnsName.indexOf("Departure Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                departureDate  = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                            }
                        } catch (Exception e) {
                            departureDate = currentCell.getDateCellValue();
                        }
                    }
//                    else if (cellIdx == columnsName.indexOf("Rate Code")) {
//                        typeName = (currentCell.getStringCellValue());
//                        rateCode = conversions.checkRateCodeExistence(rateCodes, typeName);
//                    }
                    else if (cellIdx == columnsName.indexOf("No. of Rooms")) {
                        noOfRooms = (int)currentCell.getNumericCellValue();
                    }

                    cellIdx++;
                }

                // 1=Yes, 2=No
                if (paymentAmount > 0 || transactionAmount > 0)
                    bookingDetails.cancelWithCharges = 1;
                else {
                    bookingDetails.cancelWithCharges = 2;
                }

                if (bookingDetails.cancelWithCharges == 1) {
                    serviceCharge = (roomRate * rateCode.serviceChargeRate)/100;
                    vat = ((serviceCharge + roomRate) * rateCode.vatRate)/100;
                    municipalityTax = (roomRate * rateCode.municipalityTaxRate)/100;

                    totalRoomRate = (roomRate + rateCode.basicPackageValue) * nights;
                    grandTotal = roomRate + vat + municipalityTax + serviceCharge + rateCode.basicPackageValue;
                    grandTotal = (grandTotal * nights * noOfRooms);

                    // check if there is discount percent
                    if(discount > 0)
                        bookingDetails.discount = discount;
                    else if (discountPercent > 0){
                        discount = (grandTotal * discountPercent) / 100;
                        bookingDetails.discount = discount;
                    }

                    grandTotal = grandTotal - discount;

                    bookingDetails.vat = vat;
                    bookingDetails.municipalityTax = municipalityTax;
                    bookingDetails.dailyRoomRate = roomRate + rateCode.basicPackageValue;
                    bookingDetails.totalRoomRate = totalRoomRate;
                    bookingDetails.grandTotal = grandTotal;
                    bookingDetails.chargeableDays = nights;

                    if (arrivalDate != null && departureDate != null) {
                        bookingDetails.roomRentType = conversions.checkRoomRentType(arrivalDate, departureDate);
                    }
                }

                // check if it exists in create/update booking.
                ArrayList<SyncJobData> list = syncJobDataService.getDataByBookingNoAndSyncType(bookingDetails.bookingNo,
                        newBookingSyncType.getId());
                if (list.size() > 0) {
                    bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");

                    // check if it exists in cancel booking
                    list = syncJobDataService.getDataByBookingNoAndSyncType(bookingDetails.bookingNo, syncJobType.getId());
                    if(list.size() > 0) // Update
                        bookingDetails.cuFlag = "2";
                    else // New
                        bookingDetails.cuFlag = "1";
                }

                HashMap<String, Object> data = new HashMap<>();
                Field[] allFields = bookingDetails.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    Object value = field.get(bookingDetails);
                    if (value != null && !value.equals("null")) {
                        data.put(field.getName(), value);
                    } else {
                        data.put(field.getName(), "");
                    }
                }

                SyncJobData syncJobData = new SyncJobData(data, status, reason, new Date(), syncJob.getId());
                checkCancelBookingStatus(syncJobData);
                syncJobDataList.add(syncJobData);
            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return syncJobDataList;

    }

    private void checkCancelBookingStatus(SyncJobData cancelBookingDetails){
        String status = "";
        String reason = "";
        HashMap<String, Object> data = cancelBookingDetails.getData();

        if(data.get("transactionId").equals("")){
            status = Constants.FAILED;
            reason = "Can not cancel the booking, before creating it.";
        }else{
            if(data.get("cancelWithCharges").equals("1")){
                if(data.get("vat").equals("0")){
                    status = Constants.FAILED;
                    reason = "Invalid VAT. If Cancelled with Charges then this field should not contain 0. It must be numeric (Amount) only.";
                } else if(data.get("roomRentType").equals("0")){
                    status = Constants.FAILED;
                    reason = "Invalid Room Rent Type. this field should not contain 0 & It must be numeric and available in lookup list.";
                } else if(data.get("dailyRoomRate").equals("0")){
                    status = Constants.FAILED;
                    reason = "Invalid Daily Room Rate. this field should not contain 0 & It must be numeric (Amount) only.";
                } else if(data.get("totalRoomRate").equals("0")){
                    status = Constants.FAILED;
                    reason = "Invalid Total Room Rate. this field should not contain 0 & It must be numeric (Amount) only.";
                } else if(data.get("grandTotal").equals("0")){
                    status = Constants.FAILED;
                    reason = "Invalid Grand Total. this field should not contain 0 & It must be numeric (Amount) only.";
                } else {
                    status = Constants.SUCCESS;
                }
            }else{
                status = Constants.SUCCESS;
            }
        }
        cancelBookingDetails.setReason(reason);
        cancelBookingDetails.setStatus(status);
    }

    public List<SyncJobData> getOccupancyFromExcel(SyncJob syncJob, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;

            int totalRooms = 0;
            OccupancyDetails occupancyDetails;

            ArrayList<String> columnsName = new ArrayList<>();

            while (rows.hasNext()) {
                currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    cellsInRow = currentRow.iterator();
                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        columnsName.add(currentCell.getStringCellValue().strip());
                    }
                    rowNumber++;
                    continue;
                }

                cellsInRow = currentRow.iterator();
                occupancyDetails = new OccupancyDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    if (cellIdx == columnsName.indexOf("Rooms Occupied")) {
                        occupancyDetails.roomsOccupied = (int) currentCell.getNumericCellValue();
                    } else if (cellIdx == columnsName.indexOf("Rooms Available")) {
                        occupancyDetails.roomsAvailable = (int) currentCell.getNumericCellValue();
                    } else if (cellIdx == columnsName.indexOf("Rooms On Maintenance")) {
                        occupancyDetails.roomsOnMaintenance = (int) currentCell.getNumericCellValue();
                    } else if (cellIdx == columnsName.indexOf("Total Rooms")) {
                        totalRooms = (int) currentCell.getNumericCellValue();
                    }
                    cellIdx++;
                }

                if (totalRooms == 0)
                    continue;

                occupancyDetails.roomsBooked = totalRooms -
                        (occupancyDetails.roomsOccupied + occupancyDetails.roomsAvailable + occupancyDetails.roomsOnMaintenance);

                Date updateDate = new Date();
                occupancyDetails.updateDate = dateFormat.format(updateDate);

                HashMap<String, Object> data = new HashMap<>();
                Field[] allFields = occupancyDetails.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    Object value = field.get(occupancyDetails);
                    if (value != null && !value.equals("null")) {
                        data.put(field.getName(), value);
                    } else {
                        data.put(field.getName(), "");
                    }
                }

                SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                syncJobDataList.add(syncJobData);
            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return syncJobDataList;
    }

    public List<SyncJobData> getExpensesUpdateFromExcel(SyncJob syncJob, InputStream is, GeneralSettings generalSettings,
                                                        BookingConfiguration configuration) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        float municipalityTax= 0;
        float vat = 0;
        float serviceCharge = 0;
        float unitPrice = 0;

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> expenseTypes = generalSettings.getExpenseTypes();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;

            ExpenseObject expenseObject;
            ExpenseItem expenseItem;

            String roomNo = "";
            float transactionAmount = 0;
            String typeName;
            BookingType paymentType = new BookingType();
            String transactionDescription = "";

            ArrayList<String> columnsName = new ArrayList<>();

            expenseObject = new ExpenseObject();
            expenseItem = new ExpenseItem();

            while (rows.hasNext()) {
                currentRow = rows.next();
                if (rowNumber == 0) {
                    cellsInRow = currentRow.iterator();
                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        columnsName.add(currentCell.getStringCellValue().strip());
                    }
                    rowNumber++;
                    continue;
                }

                cellsInRow = currentRow.iterator();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        String bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));

                        ArrayList<SyncJobData> list = syncJobDataService.getSyncJobDataByBookingNo(bookingNo.strip());
                        if (list.size() > 0) {
                            expenseObject.transactionId = (String) list.get(0).getData().get("transactionId");
                        } else {
                            cellIdx++;
                            continue;
                        }

                    } else if (cellIdx == columnsName.indexOf("Room No.")) {
                        try{
                            roomNo = String.valueOf(currentCell.getNumericCellValue());
                        } catch (Exception e) {
                            roomNo = currentCell.getStringCellValue();
                        }
                    } else if (cellIdx == columnsName.indexOf("Transaction Date")) {
                        Date updateDate = currentCell.getDateCellValue();
                        if (updateDate != null)
                            expenseItem.expenseDate = dateFormat.format(updateDate);
                    } else if (cellIdx == columnsName.indexOf("Transaction Amount")) {
                        transactionAmount = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        typeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkBookingTypeExistence(paymentTypes, typeName);
                    } else if (cellIdx == columnsName.indexOf("Transaction Code Description")) {
                        typeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkExpenseTypeExistence(expenseTypes, typeName);
                        transactionDescription = typeName;
                    }
                    cellIdx++;
                }

                if (transactionDescription.toLowerCase().contains("vat")) {
                    expenseObject.items.get(0).vat = String.valueOf(transactionAmount);
                    vat = transactionAmount;
                } else if (transactionDescription.toLowerCase().contains("muncipality")) {
                    municipalityTax = transactionAmount;
                    expenseObject.items.get(0).municipalityTax = String.valueOf(transactionAmount);
                    expenseItem.grandTotal = String.valueOf(unitPrice + vat + municipalityTax + serviceCharge);

                    syncJobDataList.remove(syncJobDataList.size() - 1);

                    HashMap<String, Object> data = new HashMap<>();
                    Field[] allFields = expenseObject.getClass().getDeclaredFields();
                    for (Field field : allFields) {
                        field.setAccessible(true);
                        Object value = field.get(expenseObject);
                        if (value != null && !value.equals("null")) {
                            data.put(field.getName(), value);
                        } else {
                            data.put(field.getName(), "");
                        }
                    }

                    SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                    syncJobDataList.add(syncJobData);

                    expenseObject = new ExpenseObject();
                    expenseItem = new ExpenseItem();
                } else if (transactionDescription.toLowerCase().contains("service charge")) {
                    serviceCharge = transactionAmount;
                } else {
                    expenseObject = new ExpenseObject();
                    expenseItem = new ExpenseItem();

                    expenseItem.cuFlag = "1";
                    expenseItem.discount = "0";

                    unitPrice = transactionAmount;
                    expenseObject.roomNo = roomNo;
                    expenseItem.unitPrice = String.valueOf(transactionAmount);
                    expenseItem.grandTotal = String.valueOf(transactionAmount);
                    expenseItem.paymentType = paymentType.getTypeId();
                    expenseItem.expenseTypeId = paymentType.getTypeId();

                    expenseItem.vat = "0";
                    expenseItem.municipalityTax = "0";

                    if (expenseItem.expenseTypeId != 0 && !expenseItem.unitPrice.equals("0.0")) {
                        expenseObject.items.add(expenseItem);

                        HashMap<String, Object> data = new HashMap<>();
                        Field[] allFields = expenseObject.getClass().getDeclaredFields();
                        for (Field field : allFields) {
                            field.setAccessible(true);
                            Object value = field.get(expenseObject);
                            if (value != null && !value.equals("null")) {
                                data.put(field.getName(), value);
                            } else {
                                data.put(field.getName(), "");
                            }
                        }

                        SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                        syncJobDataList.add(syncJobData);
                    }
                }

            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return syncJobDataList;
    }

    public List<SyncJobData> getExpensesUpdateFromExcelV2(SyncJob syncJob, InputStream is, GeneralSettings generalSettings,
                                                        BookingConfiguration configuration) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        float municipalityTax= 0;
        float vat = 0;
        float serviceCharge = 0;
        float municipalityTaxPercent= 0;
        float vatPercent = 0;
        float serviceChargePercent = 0;

        float unitPrice = 0;
        String generates = "";
        String[] generatesArray;

        ArrayList<String> neglectedGroupCodes = configuration.neglectedGroupCodes;
        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> expenseTypes = generalSettings.getExpenseTypes();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;

            ExpenseObject expenseObject;
            ExpenseItem expenseItem;

            String roomNo = "";
            float transactionAmount = 0;
            String typeName;
            BookingType paymentType = new BookingType();
            int transactionDescription = 0;
            String groupCodeDescription = "";

            ArrayList<String> columnsName = new ArrayList<>();

            expenseObject = new ExpenseObject();
            expenseItem = new ExpenseItem();

            while (rows.hasNext()) {
                serviceChargePercent = 0; vatPercent = 0; municipalityTaxPercent = 0;
                currentRow = rows.next();
                if (rowNumber == 0) {
                    cellsInRow = currentRow.iterator();
                    while (cellsInRow.hasNext()) {
                        Cell currentCell = cellsInRow.next();
                        columnsName.add(currentCell.getStringCellValue().strip());
                    }
                    rowNumber++;
                    continue;
                }

                cellsInRow = currentRow.iterator();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        String bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));

                        ArrayList<SyncJobData> list = syncJobDataService.getSyncJobDataByBookingNo(bookingNo.strip());
                        if (list.size() > 0) {
                            expenseObject.transactionId = (String) list.get(0).getData().get("transactionId");
                        } else {
                            cellIdx++;
                            continue;
                        }

                    } else if (cellIdx == columnsName.indexOf("Room No.")) {
                        try{
                            roomNo = String.valueOf((int)currentCell.getNumericCellValue());
                        } catch (Exception e) {
                            roomNo = currentCell.getStringCellValue();
                        }
                    } else if (cellIdx == columnsName.indexOf("Transaction Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                Date updateDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                                if (updateDate != null)
                                    expenseItem.expenseDate = dateFormat.format(updateDate);
                            }
                        } catch (Exception e) {
                            Date updateDate = currentCell.getDateCellValue();
                            if (updateDate != null)
                                expenseItem.expenseDate = dateFormat.format(updateDate);
                        }
                    } else if (cellIdx == columnsName.indexOf("Transaction Amount")) {
                        transactionAmount = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                    } else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        typeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkBookingTypeExistence(paymentTypes, typeName);
                    } else if (cellIdx == columnsName.indexOf("Transaction Code")) {
                        typeName = String.valueOf((int)currentCell.getNumericCellValue());
                        paymentType = conversions.checkExpenseTypeExistence(expenseTypes, typeName);
                        transactionDescription = paymentType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Code Group")) {
                        groupCodeDescription = (currentCell.getStringCellValue());
                    } else if (cellIdx == columnsName.indexOf("Generates")) {
                        try {
                            generates = currentCell.getStringCellValue();
                            generates = generates.replaceAll(",", "").replaceAll("\\s", "");
                            generatesArray = generates.split("%");
                            if(generatesArray.length >= 1 && !generatesArray[generatesArray.length -1].equals("")){
                                try {
                                    vatPercent = Integer.parseInt(generatesArray[generatesArray.length -1])/100;
                                } catch (Exception e) {
                                    vatPercent = 0;
                                }
                            }
                            if(generatesArray.length >= 2 && !generatesArray[generatesArray.length -2].equals(""))
                                municipalityTaxPercent = Integer.parseInt(generatesArray[generatesArray.length -2])/100;
                            if(generatesArray.length >= 3 && !generatesArray[generatesArray.length -3].equals(""))
                                serviceChargePercent = Integer.parseInt(generatesArray[generatesArray.length -3])/100;
                        } catch (Exception e) {
                            vatPercent = (float) currentCell.getNumericCellValue();
                        }
                    }
                    cellIdx++;
                }

                // Skip neglected group code
                if(neglectedGroupCodes.contains(groupCodeDescription))
                    continue;

                unitPrice = transactionAmount;

                // Round up to nearest 2 digests
                serviceCharge = conversions.roundUpFloat2Digest(transactionAmount * serviceChargePercent);
                municipalityTax = conversions.roundUpFloat2Digest(transactionAmount * municipalityTaxPercent);
                vat = conversions.roundUpFloat2Digest((transactionAmount + municipalityTax) * vatPercent);

                expenseItem.cuFlag = "1";
                expenseItem.discount = "0";

                expenseObject.roomNo = roomNo;
                expenseItem.paymentType = paymentType.getTypeId();
                expenseItem.expenseTypeId = transactionDescription;

                expenseItem.vat = String.valueOf(vat);
                expenseItem.municipalityTax = String.valueOf(municipalityTax);
                expenseItem.unitPrice = String.valueOf(transactionAmount);
                expenseItem.grandTotal = String.valueOf(unitPrice + vat + municipalityTax + serviceCharge);

                if (expenseItem.expenseTypeId != 0 && !expenseItem.unitPrice.equals("0.0")) {
                    expenseObject.items.add(expenseItem);

                    HashMap<String, Object> data = new HashMap<>();
                    Field[] allFields = expenseObject.getClass().getDeclaredFields();
                    for (Field field : allFields) {
                        field.setAccessible(true);
                        Object value = field.get(expenseObject);
                        if (value != null && !value.equals("null")) {
                            data.put(field.getName(), value);
                        } else {
                            data.put(field.getName(), "");
                        }
                    }

                    SyncJobData syncJobData = new SyncJobData(data, "success", "", new Date(), syncJob.getId());
                    syncJobDataList.add(syncJobData);
                }

                expenseObject = new ExpenseObject();
                expenseItem = new ExpenseItem();


            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return syncJobDataList;
    }
}

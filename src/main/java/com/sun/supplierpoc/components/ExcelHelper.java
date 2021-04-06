package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
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

    public List<SyncJobData> getNewBookingFromExcel(SyncJob syncJob, String municipalityTax, GeneralSettings generalSettings,
                                                    InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> roomTypes = generalSettings.getRoomTypes();
        ArrayList<BookingType> genders = generalSettings.getGenders();
        ArrayList<BookingType> nationalities = generalSettings.getNationalities();
        ArrayList<BookingType> purposeOfVisit = generalSettings.getPurposeOfVisit();
        ArrayList<BookingType> transactionTypes = generalSettings.getTransactionTypes();
        ArrayList<BookingType> customerTypes = generalSettings.getCustomerTypes();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;
            BookingDetails bookingDetails;

            String typeName;
            BookingType bookingType;

            Date arrivalDate = null;
            Date departureDate = null;

            ArrayList<String> columnsName = new ArrayList<>();

            while (rows.hasNext()) {
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
                bookingDetails = new BookingDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        bookingDetails.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Resv Status")) {
                        bookingDetails.reservationStatus = currentCell.getStringCellValue();
                    } else if (cellIdx == columnsName.indexOf("Nationality Code")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(nationalities, typeName);

                        bookingDetails.nationalityCode = bookingType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Arrival Date")) {
                        arrivalDate = currentCell.getDateCellValue();
                        bookingDetails.checkInDate = (dateFormat.format(arrivalDate));
                    } else if (cellIdx == columnsName.indexOf("Departure Date")) {
                        departureDate = currentCell.getDateCellValue();
                        bookingDetails.checkOutDate = dateFormat.format(departureDate);
                    } else if (cellIdx == columnsName.indexOf("Number of Nights")) {
                        bookingDetails.totalDurationDays = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Room No.")) {
                        bookingDetails.allotedRoomNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Room Type")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(roomTypes, typeName);

                        bookingDetails.roomType = bookingType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Full Rate Amount")) {
                        bookingDetails.totalRoomRate = String.valueOf(conversions.roundUpFloat((float) currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Total Room")) {
                        bookingDetails.grandTotal = String.valueOf((conversions.roundUpFloat((float) currentCell.getNumericCellValue())));
                    } else if (cellIdx == columnsName.indexOf("Gender")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(genders, typeName);

                        bookingDetails.gender = bookingType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Adults")) {
                        bookingDetails.noOfGuest = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Date of Birth")) {
                        bookingDetails.dateOfBirth = String.valueOf(currentCell.getDateCellValue());
                    } else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                        bookingDetails.paymentType = bookingType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("No. of Rooms")) {
                        bookingDetails.noOfRooms = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Purpose of Stay")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(purposeOfVisit, typeName);

                        bookingDetails.purposeOfVisit = bookingType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Room Tax")) {
                        bookingDetails.vat = String.valueOf((float) (currentCell.getNumericCellValue()));
                    }

                    cellIdx++;
                }

                // check if it was new booking or update
                ArrayList<SyncJobData> list = syncJobDataService.getSyncJobDataByBookingNo(bookingDetails.bookingNo);
                if (list.size() > 0) {
                    // Update
                    bookingDetails.cuFlag = "2";
                    bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");
                } else {
                    // New
                    bookingDetails.cuFlag = "1";
                    bookingDetails.transactionId = "";
                }

                bookingDetails.municipalityTax = municipalityTax;
                if (bookingDetails.discount.equals(""))
                    bookingDetails.discount = "0";

                if (arrivalDate != null && departureDate != null) {
                    bookingDetails.roomRentType = conversions.checkRoomRentType(arrivalDate, departureDate);
                }

                // Static Value
                typeName = "Room Types";
                bookingType = conversions.checkBookingTypeExistence(transactionTypes, typeName);
                bookingDetails.transactionTypeId = bookingType.getTypeId();

                typeName = "Visitor";
                bookingType = conversions.checkBookingTypeExistence(customerTypes, typeName);
                bookingDetails.customerType = bookingType.getTypeId();

                // Fetch from database
                bookingDetails.dailyRoomRate = "1000";

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

    public List<SyncJobData> getCancelBookingFromExcel(SyncJob syncJob, String municipalityTax,
                                                       ArrayList<BookingType> paymentTypes,
                                                       ArrayList<BookingType> cancelReasons, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;
            CancelBookingDetails bookingDetails;

            float paymentAmount;
            String paymentTypeName;
            BookingType paymentType;

            String cancelReasonName;
            BookingType cancelReason;

            Date arrivalDate = null;
            Date departureDate = null;

            ArrayList<String> columnsName = new ArrayList<>();

            while (rows.hasNext()) {
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
                    } else if (cellIdx == columnsName.indexOf("Cancellation Reason")) {
                        cancelReasonName = (currentCell.getStringCellValue());
                        cancelReason = conversions.checkBookingTypeExistence(cancelReasons, cancelReasonName);

                        bookingDetails.cancelReason = cancelReason.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Number of Nights")) {
                        bookingDetails.chargeableDays = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Full Rate Amount")) {
                        bookingDetails.totalRoomRate = String.valueOf(conversions.roundUpFloat((float) (currentCell.getNumericCellValue())));
                    } else if (cellIdx == columnsName.indexOf("Discount Amount")) {
                        bookingDetails.discount = String.valueOf((int) (currentCell.getNumericCellValue()));
                    } else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        paymentTypeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkBookingTypeExistence(paymentTypes, paymentTypeName);

                        bookingDetails.paymentType = paymentType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Payment Amount")) {
                        paymentAmount = (float) (currentCell.getNumericCellValue());

                        if (paymentAmount > 0)
                            bookingDetails.cancelWithCharges = "1";
                        else {
                            bookingDetails.cancelWithCharges = "0";
                        }

                    } else if (cellIdx == columnsName.indexOf("Total")) {
                        bookingDetails.grandTotal = String.valueOf(conversions.roundUpFloat((float) (currentCell.getNumericCellValue())));
                    } else if (cellIdx == columnsName.indexOf("Arrival Date")) {
                        arrivalDate = currentCell.getDateCellValue();
                    } else if (cellIdx == columnsName.indexOf("Departure Date")) {
                        departureDate = currentCell.getDateCellValue();
                    }

                    cellIdx++;
                }

                // check if it was new booking or update
                ArrayList<SyncJobData> list = syncJobDataService.getSyncJobDataByBookingNo(bookingDetails.bookingNo);
                if (list.size() > 0) {
                    // Update
                    bookingDetails.cuFlag = "2";
                    bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");
                } else {
                    // New
                    bookingDetails.cuFlag = "1";
                    bookingDetails.transactionId = "";
                }

                bookingDetails.vat = "10";

                bookingDetails.municipalityTax = municipalityTax;
                if (bookingDetails.discount.equals(""))
                    bookingDetails.discount = "0";

                if (arrivalDate != null && departureDate != null) {
                    bookingDetails.roomRentType = conversions.checkRoomRentType(arrivalDate, departureDate);
                }

                if (Float.parseFloat(bookingDetails.chargeableDays) != 0) {
                    float dailyRate = Float.parseFloat(bookingDetails.totalRoomRate) / Float.parseFloat(bookingDetails.chargeableDays);
                    bookingDetails.dailyRoomRate = String.valueOf(conversions.roundUpFloat(dailyRate));
                } else {
                    bookingDetails.dailyRoomRate = bookingDetails.totalRoomRate;
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
                        totalRooms = (int)currentCell.getNumericCellValue();
                    }
                    cellIdx++;
                }

                if(totalRooms == 0)
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

        int municipalityTaxRate = configuration.municipalityTaxRate;
        int vatRate = configuration.vatRate;
        int serviceChargeRate = configuration.serviceChargeRate;

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

            String transactionGroup = "";
            float serviceCharge;
            float transactionAmount = 0;
            String typeName;
            BookingType paymentType;

            ArrayList<String> columnsName = new ArrayList<>();

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
                expenseObject = new ExpenseObject();
                expenseItem = new ExpenseItem();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        String bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));

                        ArrayList<SyncJobData> list = syncJobDataService.getSyncJobDataByBookingNo(bookingNo.strip());
                        if (list.size() > 0) {
                            expenseObject.transactionId = (String) list.get(0).getData().get("transactionId");
                        } else {
                            cellIdx ++;
                            continue;
                        }

                    } else if (cellIdx == columnsName.indexOf("Transaction Date")) {
                        Date updateDate = currentCell.getDateCellValue();
                        if(updateDate != null)
                            expenseItem.expenseDate = dateFormat.format(updateDate);
                    } else if (cellIdx == columnsName.indexOf("Transaction Amount")) {
                        transactionAmount = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                        expenseItem.unitPrice = String.valueOf(transactionAmount);
                    } else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        typeName = (currentCell.getStringCellValue());
                        paymentType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                        expenseItem.paymentType = paymentType.getTypeId();
                    } else if (cellIdx == columnsName.indexOf("Transaction Code Description")) {
                        typeName = (currentCell.getStringCellValue());
                        if(typeName.toLowerCase().contains("vat") || typeName.toLowerCase().contains("muncipality") ||
                                typeName.toLowerCase().contains("service charge") || typeName.toLowerCase().contains("tdf")){
                            break;
                        }
                        paymentType = conversions.checkExpenseTypeExistence(expenseTypes, typeName);

                        expenseItem.expenseTypeId = paymentType.getTypeId();
                    }

                    serviceCharge = (transactionAmount * serviceChargeRate) / 100;
                    expenseItem.municipalityTax = String.valueOf(conversions.roundUpFloat((transactionAmount * municipalityTaxRate)/100));
                    expenseItem.vat = String.valueOf(conversions.roundUpFloat(((transactionAmount + serviceCharge) * vatRate)/100));

                    expenseItem.cuFlag = "1";

                    expenseItem.discount = "0";
                    expenseItem.grandTotal = String.valueOf(transactionAmount + serviceCharge);

                    cellIdx++;
                }

                if(!expenseItem.expenseTypeId.equals("") && !expenseItem.unitPrice.equals("0.0")){
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

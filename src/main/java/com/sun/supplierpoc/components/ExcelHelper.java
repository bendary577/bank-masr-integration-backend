package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
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
                        columnsName.add(currentCell.getStringCellValue());
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
                bookingDetails.transactionTypeId = "1";
                bookingDetails.customerType = "1";

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
                        columnsName.add(currentCell.getStringCellValue());
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

}

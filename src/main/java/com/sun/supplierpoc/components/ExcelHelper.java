package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.opera.booking.BookingDetails;
import com.sun.supplierpoc.models.opera.Reservation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {

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
                            }catch (Exception e){
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

                SyncJobData syncJobData = new SyncJobData(reservation, "success", "",  new Date(), syncJob.getId(), false);
                syncJobDataList.add(syncJobData);
            }
            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    public List<SyncJobData> getNewBookingFromExcel(SyncJob syncJob, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;
            BookingDetails bookingDetails;

            while (rows.hasNext()) {
                currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber += 2;
                    rows.next();
                    continue;
                }

                cellsInRow = currentRow.iterator();
                bookingDetails = new BookingDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0:
                            bookingDetails.bookingNo = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;
                        case 1:
                            bookingDetails.nationalityCode = currentCell.getStringCellValue();
                            break;
                        case 2:
                            Date arrival = currentCell.getDateCellValue();
                            bookingDetails.checkInDate = (dateFormat.format(arrival));
                            break;
                        case 3:
                            Date departure = currentCell.getDateCellValue();
                            bookingDetails.checkOutDate = dateFormat.format(departure);
                            break;
                        case 4:
                            bookingDetails.totalDurationDays = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;
                        case 5:
                            bookingDetails.allotedRoomNo = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;
                        case 6:
                            bookingDetails.roomType = currentCell.getStringCellValue();
                            break;
                        case 7:
                            bookingDetails.totalRoomRate = String.valueOf(conversions.roundUpFloat((float) currentCell.getNumericCellValue()));
                            break;
                        case 8:
                            bookingDetails.grandTotal = String.valueOf((conversions.roundUpFloat((float) currentCell.getNumericCellValue())));
                            break;
                        case 9:
                            bookingDetails.gender = currentCell.getStringCellValue();
                            break;
                        case 10:
                            bookingDetails.noOfGuest = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;
                        case 11:
                            bookingDetails.dateOfBirth = String.valueOf(currentCell.getDateCellValue());
                            break;
                        case 12:
                            bookingDetails.paymentType = currentCell.getStringCellValue();
                            break;
                        case 13:
                            bookingDetails.noOfRooms = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }

                // Static Value
                bookingDetails.transactionID = "";
                bookingDetails.transactionTypeId = "1";
                bookingDetails.cuFlag = "1";
                bookingDetails.customerType = "1";

                // Fetch from database
                bookingDetails.dailyRoomRate = "1000";
                bookingDetails.vat = "10";
                bookingDetails.municipalityTax = "5";
                bookingDetails.discount = "5";
                bookingDetails.purposeOfVisit = "1";

                HashMap<String, Object> data = new HashMap<>();
                Field[] allFields = bookingDetails.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    Object value = field.get(bookingDetails);
                    if(value != null && !value.equals("null")){
                        data.put(field.getName(), value);
                    }else{
                        data.put(field.getName(), "");
                    }
                }

                SyncJobData syncJobData = new SyncJobData(data, "success", "",  new Date(), syncJob.getId());
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

    public List<SyncJobData> getCancelBookingFromExcel(SyncJob syncJob, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            Row currentRow;
            Iterator<Cell> cellsInRow;
            BookingDetails bookingDetails;

            while (rows.hasNext()) {
                currentRow = rows.next();

                // skip header
                if (rowNumber == 0) {
                    rowNumber += 2;
                    rows.next();
                    continue;
                }

                cellsInRow = currentRow.iterator();
                bookingDetails = new BookingDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    switch (cellIdx) {
                        case 0:
                            bookingDetails.bookingNo = String.valueOf((int)(currentCell.getNumericCellValue()));
                            break;

                        default:
                            break;
                    }
                    cellIdx++;
                }

                // Static Value
                bookingDetails.transactionID = "";
                bookingDetails.transactionTypeId = "1";
                bookingDetails.cuFlag = "1";
                bookingDetails.customerType = "1";

                // Fetch from database
                bookingDetails.dailyRoomRate = "1000";
                bookingDetails.vat = "10";
                bookingDetails.municipalityTax = "5";
                bookingDetails.discount = "5";
                bookingDetails.purposeOfVisit = "1";

                HashMap<String, Object> data = new HashMap<>();
                Field[] allFields = bookingDetails.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    field.setAccessible(true);
                    Object value = field.get(bookingDetails);
                    if(value != null && !value.equals("null")){
                        data.put(field.getName(), value);
                    }else{
                        data.put(field.getName(), "");
                    }
                }

                SyncJobData syncJobData = new SyncJobData(data, "success", "",  new Date(), syncJob.getId());
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

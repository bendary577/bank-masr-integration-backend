package com.sun.supplierpoc.components;


import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.opera.booking.BookingDetails;
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.RateCode;
import com.sun.supplierpoc.models.opera.booking.Reservation;
import com.sun.supplierpoc.services.SyncJobDataService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class NewBookingExcelHelper {
    @Autowired
    SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<SyncJobData> getNewBookingFromExcel(SyncJob syncJob, GeneralSettings generalSettings,
                                                    SyncJobType syncJobType, InputStream is) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        List<SyncJobData> syncJobDataList = new ArrayList<>();
        ArrayList<BookingType> transactionTypes = generalSettings.getTransactionTypes();

        String typeName;
        BookingType bookingType;

        double basicRoomRate;
        double vat;
        double municipalityTax;
        double serviceCharge;
        double grandTotal;
        int nights = 0;

        RateCode rateCode = new RateCode();
        rateCode.serviceChargeRate = syncJobType.getConfiguration().bookingConfiguration.serviceChargeRate;
        rateCode.municipalityTaxRate = syncJobType.getConfiguration().bookingConfiguration.municipalityTaxRate;
        rateCode.vatRate = syncJobType.getConfiguration().bookingConfiguration.vatRate;
        rateCode.basicPackageValue = 20;

        BookingDetails bookingDetails = new BookingDetails();
        Reservation reservation;

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            Row currentRow;
            Iterator<Cell> cellsInRow;
            ArrayList<String> columnsName = new ArrayList<>();

            currentRow = rows.next();
            cellsInRow = currentRow.iterator();
            while (cellsInRow.hasNext()) {
                Cell currentCell = cellsInRow.next();
                columnsName.add(currentCell.getStringCellValue().toLowerCase().trim());
            }

            while (rows.hasNext()) {
                currentRow = rows.next();
                cellsInRow = currentRow.iterator();

                reservation = readReservationRow(cellsInRow, columnsName, generalSettings);

                // New Booking
                if (bookingDetails.bookingNo.equals("") || !bookingDetails.bookingNo.equals(reservation.bookingNo)) {
                    // Save old one
                    if (!bookingDetails.bookingNo.equals("")) {
                        saveBooking(bookingDetails, syncJob, syncJobType, syncJobDataList);
                    }

                    // Create new one
                    bookingDetails = new BookingDetails();

                    typeName = "RESERVED";
                    bookingType = conversions.checkBookingTypeExistence(transactionTypes, typeName);
                    bookingDetails.transactionTypeId = bookingType.getTypeId();

                    if (reservation.checkInDate != null && reservation.checkOutDate != null) {
                        nights = conversions.getNights(reservation.checkInDate, reservation.checkOutDate);
                        bookingDetails.roomRentType = conversions.checkRoomRentType(reservation.checkInDate, reservation.checkOutDate);
                    }

                    bookingDetails.bookingNo = reservation.bookingNo;
                    bookingDetails.reservationStatus = typeName;

                    bookingDetails.totalDurationDays = nights;
                    bookingDetails.noOfGuest = reservation.adults + reservation.children;

                    bookingDetails.customerType = reservation.customerType;
                    bookingDetails.nationalityCode = reservation.nationalityCode;
                    bookingDetails.purposeOfVisit = reservation.purposeOfVisit;
                    bookingDetails.dateOfBirth = reservation.dateOfBirth;
                    bookingDetails.paymentType = reservation.paymentType;

                    bookingDetails.checkInDate = dateFormat.format(reservation.checkInDate);
                    bookingDetails.checkOutDate = dateFormat.format(reservation.checkOutDate);
                }

                if(nights > 0){
                    nights --;

                    basicRoomRate = reservation.dailyRoomRate;

                    serviceCharge = (basicRoomRate * rateCode.serviceChargeRate) / 100;
                    vat = ((serviceCharge + basicRoomRate) * rateCode.vatRate) / 100;
                    municipalityTax = (basicRoomRate * rateCode.municipalityTaxRate) / 100;

                    grandTotal = basicRoomRate + vat + municipalityTax + serviceCharge + rateCode.basicPackageValue;

                    bookingDetails.vat = conversions.roundUpDouble(bookingDetails.vat + vat);
                    bookingDetails.municipalityTax = conversions.roundUpDouble(bookingDetails.municipalityTax + municipalityTax);
                    bookingDetails.grandTotal = conversions.roundUpDouble(bookingDetails.grandTotal + grandTotal);

                    bookingDetails.dailyRoomRate = conversions.roundUpDouble(basicRoomRate + rateCode.basicPackageValue);
                    bookingDetails.totalRoomRate = conversions.roundUpDouble(bookingDetails.totalRoomRate + basicRoomRate + rateCode.basicPackageValue);
                }
            }

            saveBooking(bookingDetails, syncJob, syncJobType, syncJobDataList);

            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private Reservation readReservationRow(Iterator<Cell> cellsInRow, ArrayList<String> columnsName,
                                           GeneralSettings generalSettings) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        Date time;
        String typeName;
        BookingType bookingType;

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> roomTypes = generalSettings.getRoomTypes();
        ArrayList<BookingType> genders = generalSettings.getGenders();
        ArrayList<BookingType> nationalities = generalSettings.getNationalities();
        ArrayList<BookingType> purposeOfVisit = generalSettings.getPurposeOfVisit();
        ArrayList<BookingType> customerTypes = generalSettings.getCustomerTypes();

        int cellIdx = 0;
        Reservation reservation = new Reservation();
        while (cellsInRow.hasNext()) {
            Cell currentCell = cellsInRow.next();

            if (cellIdx == columnsName.indexOf("booking no")) {
                reservation.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
            } else if (cellIdx == columnsName.indexOf("arrival date")) {
                try {
                    if (!currentCell.getStringCellValue().equals("")) {
                        reservation.checkInDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                    }
                } catch (Exception e) {
                    reservation.checkInDate = currentCell.getDateCellValue();
                }
            } else if (cellIdx == columnsName.indexOf("departure date")) {
                try {
                    if (!currentCell.getStringCellValue().equals("")) {
                        reservation.checkOutDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                    }
                } catch (Exception e) {
                    reservation.checkOutDate = currentCell.getDateCellValue();
                }
            } else if (cellIdx == columnsName.indexOf("arrival time")) {
                try {
                    if (currentCell.getStringCellValue().equals(""))
                        reservation.checkInTime = "00:00";

                    if (currentCell.getStringCellValue().contains("*"))
                        reservation.checkInTime = currentCell.getStringCellValue().replace("*", "");

                    Date date = new SimpleDateFormat("HH:mmm").parse(reservation.checkInTime);
                    reservation.checkInTime = timeFormat.format(date);

                } catch (Exception e) {
                    time = currentCell.getDateCellValue();
                    reservation.checkInTime = (timeFormat.format(time));
                }
            } else if (cellIdx == columnsName.indexOf("departure time")) {
                try {
                    if (currentCell.getStringCellValue().equals(""))
                        reservation.checkOutTime = "00:00";

                    if (currentCell.getStringCellValue().contains("*"))
                        reservation.checkOutTime = currentCell.getStringCellValue().replace("*", "");

                    Date date = new SimpleDateFormat("HH:mmm").parse(reservation.checkInTime);
                    reservation.checkOutTime = timeFormat.format(date);
                } catch (Exception e) {
                    time = currentCell.getDateCellValue();
                    reservation.checkOutTime = timeFormat.format(time);
                }
            } else if (cellIdx == columnsName.indexOf("adults")) {
                reservation.adults = (int) (currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("children")) {
                reservation.children = (int) (currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("nights")) {
                reservation.nights = (int) (currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("room no.")) {
                reservation.roomNo = String.valueOf((int) (currentCell.getNumericCellValue()));
            } else if (cellIdx == columnsName.indexOf("room type")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(roomTypes, typeName);

                reservation.roomType = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("no. of rooms")) {
                reservation.noOfRooms = (int) (currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("amount")) {
                reservation.dailyRoomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("reservation date")) {
                reservation.reservationDate = currentCell.getDateCellValue();
            } else if (cellIdx == columnsName.indexOf("ct")) {
                typeName = currentCell.getStringCellValue();
                bookingType = conversions.checkBookingTypeExistence(customerTypes, typeName);
                reservation.customerType = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("gender")) {
                typeName = currentCell.getStringCellValue();
                bookingType = conversions.checkBookingTypeExistence(genders, typeName);

                reservation.gender = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("nationality")) {
                typeName = currentCell.getStringCellValue();
                bookingType = conversions.checkBookingTypeExistence(nationalities, typeName);

                if (typeName.equals(""))
                    bookingType.setTypeId("826");

                reservation.nationalityCode = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("date of birth")) {
                time = currentCell.getDateCellValue();
                if (time != null) reservation.dateOfBirth = dateFormat.format(time);
                else reservation.dateOfBirth = "";
            } else if (cellIdx == columnsName.indexOf("pos")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(purposeOfVisit, typeName);

                reservation.purposeOfVisit = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("payment method")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                reservation.paymentType = bookingType.getTypeId();
            }
            cellIdx++;
        }

        return reservation;
    }

    private void saveBooking(BookingDetails bookingDetails, SyncJob syncJob,
                             SyncJobType syncJobType, List<SyncJobData> syncJobDataList) {
        // check if it was new booking or update
        ArrayList<SyncJobData> list = syncJobDataService.getDataByBookingNoAndSyncType(bookingDetails.bookingNo,
                syncJobType.getId());

        if (list.size() > 0) {
            // Update
            bookingDetails.cuFlag = "2";
            bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");
        } else {
            // New
            bookingDetails.cuFlag = "1";
            bookingDetails.transactionId = "";
        }

        HashMap<String, Object> data = new HashMap<>();
        Field[] allFields = bookingDetails.getClass().getDeclaredFields();
        for (Field field : allFields) {
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(bookingDetails);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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

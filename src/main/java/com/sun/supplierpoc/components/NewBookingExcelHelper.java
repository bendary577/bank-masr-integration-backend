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

    public List<SyncJobData> getNewBookingFromExcel(SyncJob syncJob, GeneralSettings generalSettings,
                                                    SyncJobType syncJobType, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> roomTypes = generalSettings.getRoomTypes();
        ArrayList<BookingType> genders = generalSettings.getGenders();
        ArrayList<BookingType> nationalities = generalSettings.getNationalities();
        ArrayList<BookingType> purposeOfVisit = generalSettings.getPurposeOfVisit();
        ArrayList<BookingType> transactionTypes = generalSettings.getTransactionTypes();
        ArrayList<BookingType> customerTypes = generalSettings.getCustomerTypes();

        String typeName;
        BookingType bookingType;

        float totalRoomRate = 0;
        float roomRate = 0;
        float vat = 0;
        float municipalityTax = 0;
        float serviceCharge = 0;
        float grandTotal = 0;
        int nights = 0;
        int noOfRooms = 0;

        RateCode rateCode = new RateCode();
        rateCode.serviceChargeRate = syncJobType.getConfiguration().bookingConfiguration.serviceChargeRate;
        rateCode.municipalityTaxRate = syncJobType.getConfiguration().bookingConfiguration.municipalityTaxRate;
        rateCode.vatRate = syncJobType.getConfiguration().bookingConfiguration.vatRate;
        rateCode.basicPackageValue = 0;

        BookingDetails bookingDetails;

        Date time;
        Date arrivalDate = null;
        Date departureDate = null;

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
                columnsName.add(currentCell.getStringCellValue().trim());
            }

            while (rows.hasNext()) {
                currentRow = rows.next();
                cellsInRow = currentRow.iterator();
                bookingDetails = new BookingDetails();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        bookingDetails.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    }
                    else if (cellIdx == columnsName.indexOf("Status")) {
                        typeName = currentCell.getStringCellValue();

                        bookingType = conversions.checkBookingTypeExistence(transactionTypes, typeName);
                        bookingDetails.transactionTypeId = bookingType.getTypeId();
                        bookingDetails.reservationStatus = typeName;
                    }
                    else if (cellIdx == columnsName.indexOf("Arrival Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                arrivalDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                                bookingDetails.checkInDate = (dateFormat.format(arrivalDate));
                            }
                        } catch (Exception e) {
                            arrivalDate = currentCell.getDateCellValue();
                            bookingDetails.checkInDate = (dateFormat.format(arrivalDate));
                        }
                    }
                    else if (cellIdx == columnsName.indexOf("Departure Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                departureDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                                bookingDetails.checkOutDate = (dateFormat.format(departureDate));
                            }
                        } catch (Exception e) {
                            departureDate = currentCell.getDateCellValue();
                            bookingDetails.checkOutDate = dateFormat.format(departureDate);
                        }
                    }
                    else if (cellIdx == columnsName.indexOf("Arrival Time")) {
                        try {
                            if (currentCell.getStringCellValue().equals(""))
                                bookingDetails.checkInTime = "00:00";

                            if (currentCell.getStringCellValue().contains("*"))
                                bookingDetails.checkInTime = currentCell.getStringCellValue().replace("*", "");

                            Date date = new SimpleDateFormat("HH:mmm").parse(bookingDetails.checkInTime);
                            bookingDetails.checkInTime = timeFormat.format(date);

                        } catch (Exception e) {
                            time = currentCell.getDateCellValue();
                            bookingDetails.checkInTime = (timeFormat.format(time));
                        }
                    }
                    else if (cellIdx == columnsName.indexOf("Departure Time")) {
                        try {
                            if (currentCell.getStringCellValue().equals(""))
                                bookingDetails.checkOutTime = "00:00";

                            if (currentCell.getStringCellValue().contains("*"))
                                bookingDetails.checkOutTime = currentCell.getStringCellValue().replace("*", "");

                            Date date = new SimpleDateFormat("HH:mmm").parse(bookingDetails.checkInTime);
                            bookingDetails.checkOutTime = timeFormat.format(date);
                        } catch (Exception e) {
                            time = currentCell.getDateCellValue();
                            bookingDetails.checkOutTime = timeFormat.format(time);
                        }
                    }

                    else if (cellIdx == columnsName.indexOf("Adults")) {
                        bookingDetails.noOfGuest = String.valueOf((int) (currentCell.getNumericCellValue()));
                    }
                    else if (cellIdx == columnsName.indexOf("Nights")) {
                        nights = (int) (currentCell.getNumericCellValue());
                        bookingDetails.totalDurationDays = String.valueOf(nights);
                    }
                    else if (cellIdx == columnsName.indexOf("Room No.")) {
                        bookingDetails.allotedRoomNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    }
                    else if (cellIdx == columnsName.indexOf("Room Type")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(roomTypes, typeName);

                        bookingDetails.roomType = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("No. of Rooms")) {
                        noOfRooms = (int) (currentCell.getNumericCellValue());
                        bookingDetails.noOfRooms = String.valueOf(noOfRooms);
                    }
                    else if (cellIdx == columnsName.indexOf("Daily Rate")) {
                        roomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                        bookingDetails.dailyRoomRate = String.valueOf(roomRate);
                    }
                    else if (cellIdx == columnsName.indexOf("Total Rate")) {
                        bookingDetails.totalRoomRate = String.valueOf(conversions.roundUpFloat((float) currentCell.getNumericCellValue()));
                    }

                    else if (cellIdx == columnsName.indexOf("CT")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(customerTypes, typeName);
                        bookingDetails.customerType = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Gender")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(genders, typeName);

                        bookingDetails.gender = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Nationality")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(nationalities, typeName);

                        if (typeName.equals(""))
                            bookingType.setTypeId("826");

                        bookingDetails.nationalityCode = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Date of Birth")) {
                        try {
                            if (currentCell.getStringCellValue().equals("XX/XX/XX"))
                                bookingDetails.dateOfBirth = "";
                        } catch (Exception e) {
                            arrivalDate = currentCell.getDateCellValue();
                            bookingDetails.dateOfBirth = (dateFormat.format(arrivalDate));
                        }
                    }

                    else if (cellIdx == columnsName.indexOf("POS")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(purposeOfVisit, typeName);

                        bookingDetails.purposeOfVisit = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Pay Method")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                        bookingDetails.paymentType = bookingType.getTypeId();
                    }
                    cellIdx++;
                }

                serviceCharge = (roomRate * rateCode.serviceChargeRate)/100;
                vat = ((serviceCharge + roomRate) * rateCode.vatRate)/100;
                municipalityTax = (roomRate * rateCode.municipalityTaxRate)/100;

                totalRoomRate = (roomRate + rateCode.basicPackageValue) * nights;
                grandTotal = roomRate + vat + municipalityTax + serviceCharge + rateCode.basicPackageValue;
                grandTotal = grandTotal * nights * noOfRooms;

                bookingDetails.vat = String.valueOf(vat);
                bookingDetails.municipalityTax = String.valueOf(municipalityTax);
                bookingDetails.dailyRoomRate = String.valueOf(roomRate + rateCode.basicPackageValue);
                bookingDetails.totalRoomRate = String.valueOf(totalRoomRate);
                bookingDetails.grandTotal = String.valueOf(grandTotal);

                if (bookingDetails.discount.equals(""))
                    bookingDetails.discount = "0";

                if (arrivalDate != null && departureDate != null) {
                    bookingDetails.roomRentType = conversions.checkRoomRentType(arrivalDate, departureDate);
                }

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

    public List<SyncJobData> getNewBooking(SyncJob syncJob, GeneralSettings generalSettings,
                                                    SyncJobType syncJobType, InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> roomTypes = generalSettings.getRoomTypes();
        ArrayList<BookingType> genders = generalSettings.getGenders();
        ArrayList<BookingType> nationalities = generalSettings.getNationalities();
        ArrayList<BookingType> purposeOfVisit = generalSettings.getPurposeOfVisit();
        ArrayList<BookingType> transactionTypes = generalSettings.getTransactionTypes();
        ArrayList<BookingType> customerTypes = generalSettings.getCustomerTypes();

        String typeName;
        BookingType bookingType;

        float totalRoomRate = 0;
        float basicRoomRate = 0;
        float roomRate = 0;
        float vat = 0;
        float municipalityTax = 0;
        float serviceCharge = 0;
        float grandTotal = 0;
        int nights = 0;
        int noOfRooms = 0;

        RateCode rateCode = new RateCode();
        rateCode.serviceChargeRate = syncJobType.getConfiguration().bookingConfiguration.serviceChargeRate;
        rateCode.municipalityTaxRate = syncJobType.getConfiguration().bookingConfiguration.municipalityTaxRate;
        rateCode.vatRate = syncJobType.getConfiguration().bookingConfiguration.vatRate;
        rateCode.basicPackageValue = 20;

        BookingDetails bookingDetails = new BookingDetails();
        Reservation reservation = new Reservation();

        Date time;
        Date arrivalDate = null;
        Date departureDate = null;

        // Package
        float packagePrice = 0;
        int packageQuantity = 0;
        String packageCalculationRule = "";

        boolean newReservation = true;

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
                columnsName.add(currentCell.getStringCellValue().trim());
            }

            while (rows.hasNext()) {
                currentRow = rows.next();
                cellsInRow = currentRow.iterator();

                if(newReservation)
                    reservation = new Reservation();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    if (cellIdx == columnsName.indexOf("Booking No")) {
                        reservation.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    }
                    else if (cellIdx == columnsName.indexOf("Arrival Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                arrivalDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                                reservation.checkInDate = (dateFormat.format(arrivalDate));
                            }
                        } catch (Exception e) {
                            arrivalDate = currentCell.getDateCellValue();
                            reservation.checkInDate = (dateFormat.format(arrivalDate));
                        }
                    }
                    else if (cellIdx == columnsName.indexOf("Departure Date")) {
                        try{
                            if (!currentCell.getStringCellValue().equals("")){
                                departureDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                                reservation.checkOutDate = (dateFormat.format(departureDate));
                            }
                        } catch (Exception e) {
                            departureDate = currentCell.getDateCellValue();
                            reservation.checkOutDate = dateFormat.format(departureDate);
                        }
                    }
                    else if (cellIdx == columnsName.indexOf("Arrival Time")) {
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
                    }
                    else if (cellIdx == columnsName.indexOf("Departure Time")) {
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
                    }
                    else if (cellIdx == columnsName.indexOf("Adults")) {
                        reservation.adults = (int) (currentCell.getNumericCellValue());
                    }
                    else if (cellIdx == columnsName.indexOf("Children")) {
                        reservation.children = (int) (currentCell.getNumericCellValue());
                    }
                    else if (cellIdx == columnsName.indexOf("Nights")) {
                        nights = (int) (currentCell.getNumericCellValue());
                        reservation.nights = String.valueOf(nights);
                    }
                    else if (cellIdx == columnsName.indexOf("Room No.")) {
                        reservation.roomNo = String.valueOf((int) (currentCell.getNumericCellValue()));
                    }
                    else if (cellIdx == columnsName.indexOf("Room Type")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(roomTypes, typeName);

                        reservation.roomType = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("No. of Rooms")) {
                        noOfRooms = (int) (currentCell.getNumericCellValue());
                        reservation.noOfRooms = String.valueOf(noOfRooms);
                    }
                    else if (cellIdx == columnsName.indexOf("Base Rate Amount")) {
                        roomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
                        reservation.dailyRoomRate = String.valueOf(roomRate);
                    }

                    // Packages
                    else if (cellIdx == columnsName.indexOf("Price")) {
                        packagePrice = (float) (currentCell.getNumericCellValue());
                    }
                    else if (cellIdx == columnsName.indexOf("Quantity")) {
                        packageQuantity = (int) (currentCell.getNumericCellValue());
                    }
                    else if (cellIdx == columnsName.indexOf("Calculation Rule")) {
                        packageCalculationRule = currentCell.getStringCellValue();
                    }

                    else if (cellIdx == columnsName.indexOf("CT")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(customerTypes, typeName);
                        reservation.customerType = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Gender")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(genders, typeName);

                        reservation.gender = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Nationality")) {
                        typeName = currentCell.getStringCellValue();
                        bookingType = conversions.checkBookingTypeExistence(nationalities, typeName);

                        if (typeName.equals(""))
                            bookingType.setTypeId("826");

                        reservation.nationalityCode = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Date of Birth")) {
                        try {
                            if (currentCell.getStringCellValue().equals("XX/XX/XX"))
                                reservation.dateOfBirth = "";
                        } catch (Exception e) {
                            arrivalDate = currentCell.getDateCellValue();
                            reservation.dateOfBirth = (dateFormat.format(arrivalDate));
                        }
                    }

                    else if (cellIdx == columnsName.indexOf("POS")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(purposeOfVisit, typeName);

                        reservation.purposeOfVisit = bookingType.getTypeId();
                    }
                    else if (cellIdx == columnsName.indexOf("Payment Method")) {
                        typeName = (currentCell.getStringCellValue());
                        bookingType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                        reservation.paymentType = bookingType.getTypeId();
                    }
                    cellIdx++;
                }

                if(newReservation){
                    bookingDetails = new BookingDetails();

                    typeName = "RESERVED";
                    bookingType = conversions.checkBookingTypeExistence(transactionTypes, typeName);
                    bookingDetails.transactionTypeId = bookingType.getTypeId();
                    bookingDetails.reservationStatus = typeName;

                    nights = conversions.getNights(arrivalDate, departureDate);
                    bookingDetails.totalDurationDays = String.valueOf(nights);

                    if (bookingDetails.discount.equals(""))
                        bookingDetails.discount = "0";

                    if (arrivalDate != null && departureDate != null) {
                        bookingDetails.roomRentType = conversions.checkRoomRentType(arrivalDate, departureDate);
                    }

                    // subtract package value to get basic daily room rate
                    basicRoomRate = roomRate;
                    basicRoomRate = basicRoomRate - (packagePrice * packageQuantity);
                } else{
                    serviceCharge = (roomRate * rateCode.serviceChargeRate)/100;
                    vat = ((serviceCharge + roomRate) * rateCode.vatRate)/100;
                    municipalityTax = (roomRate * rateCode.municipalityTaxRate)/100;

                    totalRoomRate = (roomRate + rateCode.basicPackageValue) * nights;
                    grandTotal = roomRate + vat + municipalityTax + serviceCharge + rateCode.basicPackageValue;
                    grandTotal = grandTotal * nights * noOfRooms;

                    bookingDetails.vat = String.valueOf(vat);
                    bookingDetails.municipalityTax = String.valueOf(municipalityTax);
                    bookingDetails.dailyRoomRate = String.valueOf(roomRate + rateCode.basicPackageValue);
                    bookingDetails.totalRoomRate = String.valueOf(totalRoomRate);
                    bookingDetails.grandTotal = String.valueOf(grandTotal);


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

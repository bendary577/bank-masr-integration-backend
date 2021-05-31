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

/*

Get booking details from OPERA database

SELECT
  CASE WHEN name.birth_date is NULL THEN '' END as birth,
  name.gender,
  name.nationality,
  reservation_name.confirmation_no as booking_no,
  reservation_name.arrival_date_time as arrival_date,
  reservation_name.departure_date_time as departure_date,
  reservation_name.UDFC03 as CT,
  reservation_name.UDFC05 as POS,
  reservation_name.payment_method as pm,
  reservation_name.resv_status as status,
  reservation_daily_elements.room,
  room.description,
  reservation_daily_elements.quantity,
  reservation_daily_element_name.reservation_date as res_date,
  reservation_daily_element_name.base_rate_amount as amount,
  CASE WHEN reservation_daily_element_name.discount_amt is NULL THEN 0 END as disc,
  CASE WHEN reservation_daily_element_name.discount_prcnt is NULL THEN 0 END as disc_prcnt,
  reservation_daily_element_name.adults,
  reservation_daily_element_name.children
FROM
  reservation_name
  INNER JOIN name ON reservation_name.name_id = name.name_id
  INNER JOIN reservation_daily_element_name ON reservation_name.resv_name_id = reservation_daily_element_name.resv_name_id
  inner join reservation_daily_elements on reservation_daily_element_name.resv_daily_el_seq = reservation_daily_elements.resv_daily_el_seq
  LEFT join room on reservation_daily_elements.room = room.room
WHERE
  reservation_name.update_date >= trunc(sysdate)
  And reservation_name.update_date < trunc(sysdate) + 1
  and (
    reservation_name.resv_status = 'RESERVED'
    or reservation_name.resv_status = 'CHECKED IN'
    or reservation_name.resv_status = 'CHECKED OUT'
  )
ORDER BY
  reservation_daily_element_name.reservation_date;

* */

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
                        bookingDetails.grandTotal = conversions.roundUpDouble(bookingDetails.grandTotal * bookingDetails.noOfRooms);
                        saveBooking(bookingDetails, syncJob, syncJobType, syncJobDataList);
                    }

                    // Create new one
                    bookingDetails = new BookingDetails();

                    typeName = reservation.reservationStatus;
                    bookingType = conversions.checkBookingTypeExistence(transactionTypes, typeName);
                    bookingDetails.transactionTypeId = bookingType.getTypeId();

                    if (reservation.checkInDate != null && reservation.checkOutDate != null) {
                        nights = conversions.getNights(reservation.checkInDate, reservation.checkOutDate);
                        bookingDetails.roomRentType = conversions.checkRoomRentType(reservation.checkInDate, reservation.checkOutDate);
                    }

                    bookingDetails.bookingNo = reservation.bookingNo;
                    bookingDetails.reservationStatus = typeName;

                    bookingDetails.allotedRoomNo = reservation.roomNo;
                    bookingDetails.noOfRooms = reservation.noOfRooms;
                    bookingDetails.roomType = reservation.roomType;
                    bookingDetails.totalDurationDays = nights;
                    bookingDetails.noOfGuest = reservation.adults + reservation.children;

                    bookingDetails.gender = reservation.gender;
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

            bookingDetails.grandTotal = conversions.roundUpDouble(bookingDetails.grandTotal * bookingDetails.noOfRooms);
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
            } else if (cellIdx == columnsName.indexOf("status")) {
                reservation.reservationStatus = currentCell.getStringCellValue();
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
            }

            else if (cellIdx == columnsName.indexOf("room")) {
                reservation.roomNo = String.valueOf((int) (currentCell.getNumericCellValue()));
            } else if (cellIdx == columnsName.indexOf("description")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(roomTypes, typeName);

                reservation.roomType = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("quantity")) {
                reservation.noOfRooms = (int) (currentCell.getNumericCellValue());
            }


            else if (cellIdx == columnsName.indexOf("amount")) {
                reservation.dailyRoomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("res date")) {
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

                reservation.nationalityCode = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("birth")) {
                time = currentCell.getDateCellValue();
                if (time != null) reservation.dateOfBirth = dateFormat.format(time);
                else reservation.dateOfBirth = "";
            } else if (cellIdx == columnsName.indexOf("pos")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(purposeOfVisit, typeName);

                reservation.purposeOfVisit = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("pm")) {
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

        boolean createUpdateFlag;

        if (list.size() > 0) {
            // Update
            // Check if there is any changes
            bookingDetails.cuFlag = "2";
            bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");
            createUpdateFlag = checkChanges(bookingDetails, list.get(0));
        } else {
            // New
            bookingDetails.cuFlag = "1";
            bookingDetails.transactionId = "";
            createUpdateFlag = true;
        }

        if(createUpdateFlag){
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

    private boolean checkChanges(BookingDetails bookingDetails, SyncJobData data){
        if (bookingDetails.transactionTypeId != (int) data.getData().get("transactionTypeId"))
            return true;
        else if (!bookingDetails.checkInDate.equals(data.getData().get("checkInDate")))
            return true;
        else if (!bookingDetails.checkOutDate.equals(data.getData().get("checkOutDate")))
            return true;
        else if (!bookingDetails.checkInTime.equals(data.getData().get("checkInTime")))
            return true;
        else if (!bookingDetails.checkOutTime.equals(data.getData().get("checkOutTime")))
            return true;

        else if (bookingDetails.totalDurationDays != (int) data.getData().get("totalDurationDays"))
            return true;
        else if (!bookingDetails.allotedRoomNo.equals(data.getData().get("allotedRoomNo")))
            return true;
        else if (!bookingDetails.roomRentType.equals(data.getData().get("roomRentType")))
            return true;
        else if (bookingDetails.roomType != (int) data.getData().get("roomType"))
            return true;
        else if (bookingDetails.noOfRooms != (int) data.getData().get("noOfRooms"))
            return true;
        else if (bookingDetails.noOfGuest != (int) data.getData().get("noOfGuest"))
            return true;

        else if(bookingDetails.nationalityCode != (int) data.getData().get("nationalityCode"))
            return true;
        else if (bookingDetails.gender != (int) data.getData().get("gender"))
            return true;
        else if (bookingDetails.customerType != (int) data.getData().get("customerType"))
            return true;
        else if (bookingDetails.purposeOfVisit != (int) data.getData().get("purposeOfVisit"))
            return true;
        else if (!bookingDetails.dateOfBirth.equals(data.getData().get("dateOfBirth")))
            return true;

        else if (bookingDetails.dailyRoomRate != (double) data.getData().get("dailyRoomRate"))
            return true;
        else if (bookingDetails.totalRoomRate != (double) data.getData().get("totalRoomRate"))
            return true;
        else if (bookingDetails.vat != (double) data.getData().get("vat"))
            return true;
        else if (bookingDetails.municipalityTax != (double) data.getData().get("municipalityTax"))
            return true;
        else if (bookingDetails.discount != (double) data.getData().get("discount"))
            return true;

        else return bookingDetails.paymentType != (int) data.getData().get("paymentType");
    }
}

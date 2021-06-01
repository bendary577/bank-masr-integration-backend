package com.sun.supplierpoc.components;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.opera.booking.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
SELECT
  reservation_name.confirmation_no as booking_no,
  reservation_name.arrival_date_time as arrival_date,
  reservation_name.departure_date_time as departure_date,
  reservation_name.cancellation_date as cancellation_date,
  reservation_name.resv_status as status,
  reservation_name.payment_method as pm,
  reservation_name.cancellation_reason_code as cancel_reason,
  reservation_daily_element_name.reservation_date as res_date,
  reservation_daily_element_name.base_rate_amount as amount,
  CASE WHEN reservation_daily_element_name.discount_amt is NULL THEN 0 END as disc,
  CASE WHEN reservation_daily_element_name.discount_prcnt is NULL THEN 0 END as disc_prcnt
FROM
  reservation_name
  INNER JOIN name ON reservation_name.name_id = name.name_id
  INNER JOIN reservation_daily_element_name ON reservation_name.resv_name_id = reservation_daily_element_name.resv_name_id
WHERE   reservation_name.update_date >= trunc(sysdate)
  And reservation_name.update_date < trunc(sysdate) + 1
  And reservation_name.resv_status = 'CANCELLED'
ORDER BY
  reservation_daily_element_name.reservation_date;
*/

@Service
public class CancelBookingExcelHelper {
    @Autowired
    SyncJobDataService syncJobDataService;

    private Conversions conversions = new Conversions();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<SyncJobData> getCancelBookingFromExcel(SyncJob syncJob, GeneralSettings generalSettings,
                                                    SyncJobType syncJobType, SyncJobType newBookingSyncType,
                                                       InputStream is) {
        List<SyncJobData> syncJobDataList = new ArrayList<>();

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
        rateCode.basicPackageValue = 0;

        CancelBookingDetails bookingDetails = new CancelBookingDetails();
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
//                columnsName.add(currentCell.getStringCellValue().toLowerCase().trim());
                columnsName.add(conversions.transformColName(currentCell.getStringCellValue().toLowerCase().trim()));
            }

            while (rows.hasNext()) {
                currentRow = rows.next();
                cellsInRow = currentRow.iterator();

                reservation = readReservationRow(cellsInRow, columnsName, generalSettings);

                // New Booking
                if (bookingDetails.bookingNo.equals("") || !bookingDetails.bookingNo.equals(reservation.bookingNo)) {
                    // Save old one
                    if (!bookingDetails.bookingNo.equals("")) {
                        // check if it cancel with charges
                        if(bookingDetails.cancelWithCharges == 2){
                            bookingDetails.vat = 0;
                            bookingDetails.municipalityTax = 0;
                            bookingDetails.dailyRoomRate = 0;
                            bookingDetails.totalRoomRate = 0;
                            bookingDetails.grandTotal = 0;
                            bookingDetails.chargeableDays = 0;
                            bookingDetails.roomRentType = "0";
                            bookingDetails.paymentType = 0;
                        }

                        saveBooking(bookingDetails, syncJob, syncJobType, newBookingSyncType, syncJobDataList);
                    }

                    // Create new one
                    bookingDetails = new CancelBookingDetails();

                    if (reservation.checkInDate != null && reservation.checkOutDate != null) {
                        nights = conversions.getNights(reservation.checkInDate, reservation.checkOutDate);
                        bookingDetails.roomRentType = conversions.checkRoomRentType(reservation.checkInDate, reservation.checkOutDate);
                    }

                    bookingDetails.bookingNo = reservation.bookingNo;
                    bookingDetails.paymentType = reservation.paymentType;
                }

                if(nights > 0){
                    nights --;

                    basicRoomRate = reservation.dailyRoomRate;

                    serviceCharge = (basicRoomRate * rateCode.serviceChargeRate) / 100;
                    municipalityTax = (basicRoomRate * rateCode.municipalityTaxRate) / 100;

                    vat = ((municipalityTax + basicRoomRate) * rateCode.vatRate) / 100;

                    grandTotal = basicRoomRate + vat + municipalityTax + serviceCharge + rateCode.basicPackageValue;

                    bookingDetails.vat = conversions.roundUpDouble(bookingDetails.vat + vat);
                    bookingDetails.municipalityTax = conversions.roundUpDouble(bookingDetails.municipalityTax + municipalityTax);
                    bookingDetails.grandTotal = conversions.roundUpDouble(bookingDetails.grandTotal + grandTotal);

                    bookingDetails.dailyRoomRate = conversions.roundUpDouble(basicRoomRate + rateCode.basicPackageValue);
                    bookingDetails.totalRoomRate = conversions.roundUpDouble(bookingDetails.totalRoomRate + basicRoomRate + rateCode.basicPackageValue);
                }
            }

            if(bookingDetails.cancelWithCharges == 2){
                bookingDetails.vat = 0;
                bookingDetails.municipalityTax = 0;
                bookingDetails.dailyRoomRate = 0;
                bookingDetails.totalRoomRate = 0;
                bookingDetails.grandTotal = 0;
                bookingDetails.chargeableDays = 0;
                bookingDetails.roomRentType = "0";
                bookingDetails.paymentType = 0;
            }

            saveBooking(bookingDetails, syncJob, syncJobType, newBookingSyncType, syncJobDataList);

            workbook.close();

            return syncJobDataList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private Reservation readReservationRow(Iterator<Cell> cellsInRow, ArrayList<String> columnsName,
                                           GeneralSettings generalSettings) {
        String typeName;
        BookingType bookingType;

        ArrayList<BookingType> paymentTypes = generalSettings.getPaymentTypes();
        ArrayList<BookingType> cancelReasons = generalSettings.getCancelReasons();

        int cellIdx = 0;
        Reservation reservation = new Reservation();
        while (cellsInRow.hasNext()) {
            Cell currentCell = cellsInRow.next();

            if (cellIdx == columnsName.indexOf("booking_no")) {
                reservation.bookingNo = String.valueOf((int) (currentCell.getNumericCellValue()));
            }  else if (cellIdx == columnsName.indexOf("status")) {
                reservation.reservationStatus = currentCell.getStringCellValue();
            } else if (cellIdx == columnsName.indexOf("arrival_date")) {
                try {
                    if (!currentCell.getStringCellValue().equals("")) {
                        try{
                            reservation.checkInDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                        } catch (ParseException e) {
                            // 03-DEC-20
                            reservation.checkInDate = new SimpleDateFormat("dd-MMMM-yy").parse(currentCell.getStringCellValue());
                        }
                    }
                } catch (Exception e) {
                    reservation.checkInDate = currentCell.getDateCellValue();
                }
            } else if (cellIdx == columnsName.indexOf("departure_date")) {
                try {
                    if (!currentCell.getStringCellValue().equals("")) {
                        try{
                            reservation.checkOutDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                        } catch (ParseException e) {
                            // 03-DEC-20
                            reservation.checkOutDate = new SimpleDateFormat("dd-MMMM-yy").parse(currentCell.getStringCellValue());
                        }
                    }
                } catch (Exception e) {
                    reservation.checkOutDate = currentCell.getDateCellValue();
                }
            } else if (cellIdx == columnsName.indexOf("cancellation_date")) {
//                try {
//                    if (!currentCell.getStringCellValue().equals("")) {
//                        try{
//                            reservation.reservationDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
//                        } catch (ParseException e) {
//                            // 03-DEC-20
//                            reservation.reservationDate = new SimpleDateFormat("dd-MMMM-yy").parse(currentCell.getStringCellValue());
//                        }
//                    }
//                } catch (Exception e) {
//                    reservation.checkOutDate = currentCell.getDateCellValue();
//                }
            } else if (cellIdx == columnsName.indexOf("res_date")) {
                try {
                    if (!currentCell.getStringCellValue().equals("")) {
                        try{
                            reservation.reservationDate = new SimpleDateFormat("dd.MM.yy").parse(currentCell.getStringCellValue());
                        } catch (ParseException e) {
                            // 03-DEC-20
                            reservation.reservationDate = new SimpleDateFormat("dd-MMMM-yy").parse(currentCell.getStringCellValue());
                        }
                    }
                } catch (Exception e) {
                    reservation.reservationDate = currentCell.getDateCellValue();
                }
            } else if (cellIdx == columnsName.indexOf("Cancel_reason")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(cancelReasons, typeName);

                reservation.cancelReason = bookingType.getTypeId();
            } else if (cellIdx == columnsName.indexOf("amount")) {
                reservation.dailyRoomRate = conversions.roundUpFloat((float) currentCell.getNumericCellValue());
            } else if (cellIdx == columnsName.indexOf("pm")) {
                typeName = (currentCell.getStringCellValue());
                bookingType = conversions.checkBookingTypeExistence(paymentTypes, typeName);

                reservation.paymentType = bookingType.getTypeId();
            }

            cellIdx++;
        }

        // 1=Yes, 2=No
//        if (paymentAmount > 0 || transactionAmount > 0)
//            reservation.cancelWithCharges = 1;
//        else {
//            reservation.cancelWithCharges = 2;
//        }
        reservation.cancelWithCharges = 1;

        return reservation;
    }

    private void saveBooking(CancelBookingDetails bookingDetails, SyncJob syncJob,
                             SyncJobType syncJobType, SyncJobType newBookingSyncType,
                             List<SyncJobData> syncJobDataList) {
        // Get booking number
        ArrayList<SyncJobData> list = syncJobDataService.getDataByBookingNoAndSyncType(bookingDetails.bookingNo,
                newBookingSyncType.getId());

        if (list.size() > 0)
            bookingDetails.transactionId = (String) list.get(0).getData().get("transactionId");
        else
            bookingDetails.transactionId = "";

        // Check it was create or update
        boolean createUpdateFlag;
        list = syncJobDataService.getDataByBookingNoAndSyncType(bookingDetails.bookingNo,
                syncJobType.getId());
        if (list.size() > 0){
            bookingDetails.cuFlag = "2";
            createUpdateFlag = checkChanges(bookingDetails, list.get(0));
        } else{
            bookingDetails.cuFlag = "1";
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
            checkCancelBookingStatus(syncJobData);
            syncJobDataList.add(syncJobData);
        }
    }

    private boolean checkChanges(CancelBookingDetails bookingDetails, SyncJobData data){
        if (!bookingDetails.roomRentType.equals(data.getData().get("roomRentType")))
            return true;

        else return bookingDetails.paymentType != (int) data.getData().get("paymentType");
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

}

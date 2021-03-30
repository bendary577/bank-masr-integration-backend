package com.sun.supplierpoc.models.opera.booking;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CancelBookingDetails {
    public String transactionId = "";
    public String bookingNo = "";

    public String cancelReason = "";
    public String cancelWithCharges = "";
    public String chargeableDays = "";
    public String roomRentType = "";
    public String dailyRoomRate = "";
    public String totalRoomRate = "";
    public String vat = "";
    public String municipalityTax = "";
    public String discount = "";
    public String grandTotal = "";
    public String paymentType = "";
    public String cuFlag = "";

    public String checkRoomRentType(Date arrivalDate, Date departureDate){
        long diff = departureDate.getTime() - arrivalDate.getTime();
        long numberOfDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        System.out.println ("Days: " + numberOfDays);

        if(numberOfDays % 7 == 0){
            return "3"; //Weekly
        }else if(numberOfDays % 30 == 0){
            return "4"; // Monthly
        }

        return "1"; //Daily
    }
}

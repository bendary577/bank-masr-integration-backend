package com.sun.supplierpoc.models.opera.booking;

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

    public String checkRoomRentType(){
        /*
        * 1 Daily
        * 2 Hourly
        * 3 Weekly
        * 4 Monthly
        * */

        return "";
    }
}

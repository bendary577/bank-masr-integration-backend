package com.sun.supplierpoc.models.opera.booking;
public class CancelBookingDetails {
    public String transactionId = "";
    public String bookingNo = "";

    public int cancelReason = 0;
    public int cancelWithCharges = 2;
    public int chargeableDays = 0;
    public String roomRentType = "0";
    public double dailyRoomRate = 0.0;
    public double totalRoomRate = 0.0;
    public double vat = 0.0;
    public double municipalityTax = 0.0;
    public double discount = 0.0;
    public double grandTotal = 0.0;
    public int paymentType = 0;
    public String cuFlag = "";
}

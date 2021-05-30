package com.sun.supplierpoc.models.opera.booking;

public class BookingDetails {
    public String transactionId = "";
    public int transactionTypeId = 0;

    public String bookingNo = "";

    public String checkInDate = "";
    public String checkOutDate = "";
    public String checkInTime = "";
    public String checkOutTime = "";

    public int totalDurationDays = 0;
    public String allotedRoomNo = "";
    public String roomRentType = "";
    public Double dailyRoomRate = 0.0;
    public Double totalRoomRate = 0.0;

    public Double vat = 0.0;
    public Double municipalityTax = 0.0;
    public Double discount = 0.0;

    public Double grandTotal = 0.0;

    public int gender = 0;
    public int customerType = 0;
    public int roomType = 0;
    public int purposeOfVisit = 0;
    public int nationalityCode = 0;
    public String dateOfBirth = "";

    public int paymentType = 0;

    public int noOfRooms = 0;
    public int noOfGuest = 0;
    public String reservationStatus = "";

    public String cuFlag = "";
}

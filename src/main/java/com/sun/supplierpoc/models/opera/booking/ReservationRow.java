package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;
import java.util.Date;

public class ReservationRow {
    public String bookingNo = "";

    public String checkInDate ;
    public String checkOutDate ;
    public String checkInTime = "";
    public String checkOutTime = "";
    public String cancellationDate = "";

    public String reservNameId = "";
    public String reservationStatus = "";

    public int nights = 0;
    public int roomNo = 0;
    public String roomRentType = "";
    public double dailyRoomRate = 0.0;
    public double totalRoomRate = 0.0;
    public String roomType = "";

    public String gender = "";
    public String customerType = "";
    public String purposeOfVisit = "";
    public String dateOfBirth = "";
    public String  nationalityCode = "";

    public String paymentType = "";

    public int noOfRooms = 0;
    public int adults = 0;
    public int children = 0;

    public Double vat = 0.0;
    public Double municipalityTax = 0.0;
    public Double discount = 0.0;

    public Double grandTotal = 0.0;

    public String cancelReason = "";
    public int cancelWithCharges = 0;

    public ArrayList<Package> packages = new ArrayList<>();
}

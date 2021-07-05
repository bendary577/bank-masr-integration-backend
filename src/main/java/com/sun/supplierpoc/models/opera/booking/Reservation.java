package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;
import java.util.Date;

public class Reservation {
    public String bookingNo = "";

    public Date checkInDate ;
    public Date checkOutDate ;
    public String checkInTime = "";
    public String checkOutTime = "";
    public Date reservationDate;
    public String reservationStatus = "";

    public int nights = 0;
    public int roomNo = 0;
    public String roomRentType = "";
    public double dailyRoomRate = 0.0;
    public int roomType = 0;

    public int gender = 0;
    public int customerType = 0;
    public int purposeOfVisit = 0;
    public String dateOfBirth = "";
    public int nationalityCode = 0;

    public int paymentType = 0;

    public int noOfRooms = 0;
    public int adults = 0;
    public int children = 0;

    public int cancelReason = 0;
    public int cancelWithCharges = 2;

    public ArrayList<Package> packages = new ArrayList<>();
    public int lastIndex = 0;
}

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

    public int nights = 0;
    public String roomNo = "";
    public String roomRentType = "";
    public double dailyRoomRate = 0.0;
    public String roomType = "";

    public String gender = "";
    public String customerType = "";
    public String purposeOfVisit = "";
    public String dateOfBirth = "";
    public String nationalityCode = "";

    public String paymentType = "";

    public int noOfRooms = 0;
    public int adults = 0;
    public int children = 0;
    public ArrayList<Double> packages = new ArrayList<>();
}

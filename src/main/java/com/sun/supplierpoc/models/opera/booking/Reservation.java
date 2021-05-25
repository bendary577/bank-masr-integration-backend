package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;

public class Reservation {
    public String bookingNo = "";

    public String checkInDate = "";
    public String checkOutDate = "";
    public String checkInTime = "";
    public String checkOutTime = "";

    public String nights = "";
    public String roomNo = "";
    public String roomRentType = "";
    public String dailyRoomRate = "";
    public String roomType = "";

    public String gender = "";
    public String customerType = "";
    public String purposeOfVisit = "";
    public String dateOfBirth = "";
    public String nationalityCode = "";

    public String paymentType = "";

    public String noOfRooms = "";
    public int adults = 0;
    public int children = 0;
    public ArrayList<Double> packages = new ArrayList<>();
}

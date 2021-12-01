package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;

public class ExpenseObject {
    public String transactionId = "";
    public String bookingNo = "";
    public int roomNo = 0;
    public String channel = "";

    public ArrayList<ExpenseItem> items = new ArrayList<>();
}

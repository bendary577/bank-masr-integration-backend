package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;
import java.util.Date;

public class Package {
    public String packageName = "";

    // (price * quantity)
    public double price = 0;
    public String calculationRule = "";
    public Date consumptionDate;

    public double serviceCharge = 0.0;
    public double municipalityTax = 0.0;
    public double vat = 0.0;

    public ArrayList<Generate> generates = new ArrayList<>();
    public int lastIndex = 0;
}


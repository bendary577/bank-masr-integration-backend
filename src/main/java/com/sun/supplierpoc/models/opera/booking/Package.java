package com.sun.supplierpoc.models.opera.booking;

import java.util.ArrayList;
import java.util.Date;

public class Package {
    public String packageName = "";

    // (price * quantity)
    public float price = 0;
    public String calculationRule = "";
    public Date consumptionDate;

    public Double serviceCharge = 0.0;
    public Double municipalityTax = 0.0;
    public Double vat = 0.0;

    public ArrayList<Generate> generates = new ArrayList<>();
}


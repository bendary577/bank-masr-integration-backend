package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class BookingConfiguration {
    public String filePath = "";
    public String municipalityTax = "";
    public int municipalityTaxRate = 0;
    public int vatRate = 0;
    public int serviceChargeRate= 0;

    public ArrayList<String> neglectedGroupCodes = new ArrayList<>();

}


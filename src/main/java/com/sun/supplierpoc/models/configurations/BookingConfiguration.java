package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class BookingConfiguration {
    public String fileBaseName = "";
    public String fileExtension = "";

    public int vatRate = 0;
    public int municipalityTaxRate = 0;
    public int serviceChargeRate= 0;

    public ArrayList<String> neglectedGroupCodes = new ArrayList<>();
    public ArrayList<String> neglectedRoomTypes = new ArrayList<>();

}


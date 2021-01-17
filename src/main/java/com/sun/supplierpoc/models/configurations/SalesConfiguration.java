package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class SalesConfiguration {
    public boolean tenderIncludeTax = true;
    public boolean majorGroupDiscount = false;

    public String revenue = "";
    public String vatOut = "";
    public String cashShortagePOS = "";
    public String cashSurplusPOS = "";
    public String grossDiscountSales = "";

    public ArrayList<Tax> taxes = new ArrayList<>();
    public ArrayList<Tender> tenders = new ArrayList<>();
    public ArrayList<Discount> discounts = new ArrayList<>();
    public ArrayList<ServiceCharge> serviceCharges = new ArrayList<>();
    public ArrayList<MajorGroup> majorGroups = new ArrayList<>();
}

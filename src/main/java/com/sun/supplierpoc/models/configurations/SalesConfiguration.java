package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;

public class SalesConfiguration implements Serializable{
    public String revenue = "";
    public String cashShortagePOS = "";
    public String cashSurplusPOS = "";
    public String grossDiscountSales = "";

    public boolean taxIncluded = true;
    public boolean syncTotalTax = true;
    public String totalTaxAccount = "";
    public ArrayList<Tax> taxes = new ArrayList<>();

    public ArrayList<Tender> tenders = new ArrayList<>();

    public boolean syncTotalDiscounts = true;
    public String totalDiscountsAccount = "";
    public ArrayList<Discount> discounts = new ArrayList<>();

    public boolean syncTotalServiceCharge = true;
    public String totalServiceChargeAccount = "";
    public ArrayList<ServiceCharge> serviceCharges = new ArrayList<>();

    /*
    * Include Major Group Discount or Not
    * */
    public boolean MGDiscount = false;
    public boolean RVDiscount = false;

    /*
    * sync based on major groups or family groups
    * */
    public boolean syncMG = true;

    public ArrayList<MajorGroup> majorGroups = new ArrayList<>();
}

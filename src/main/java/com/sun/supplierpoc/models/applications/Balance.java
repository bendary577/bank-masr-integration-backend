package com.sun.supplierpoc.models.applications;

import com.sun.supplierpoc.models.configurations.RevenueCenter;

import java.util.ArrayList;
import java.util.List;

public class Balance {

    private double amount;
    private List<RevenueCenter> revenueCenters = new ArrayList<>();

    public Balance() {
    }

    public Balance(double amount, List<RevenueCenter> revenueCenters) {
        this.amount = amount;
        this.revenueCenters = revenueCenters;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(List<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }
}

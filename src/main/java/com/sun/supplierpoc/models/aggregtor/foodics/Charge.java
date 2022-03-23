package com.sun.supplierpoc.models.aggregtor.foodics;

import java.util.ArrayList;
import java.util.List;

public class Charge {
    public int amount;
    public String charge_id;
    public ArrayList<Tax> taxes;

    public String getCharge_id() {
        return charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ArrayList<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<Tax> taxes) {
        this.taxes = taxes;
    }
}

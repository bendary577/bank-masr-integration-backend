package com.sun.supplierpoc.models.aggregtor.foodics;


import java.util.ArrayList;
import java.util.List;

public class Option {
    public String modifier_option_id;
    public int quantity;
    public double unit_price;
    public ArrayList<Tax> taxes;

    public String getModifier_option_id() {
        return modifier_option_id;
    }

    public void setModifier_option_id(String modifier_option_id) {
        this.modifier_option_id = modifier_option_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(double unit_price) {
        this.unit_price = unit_price;
    }

    public ArrayList<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<Tax> taxes) {
        this.taxes = taxes;
    }
}


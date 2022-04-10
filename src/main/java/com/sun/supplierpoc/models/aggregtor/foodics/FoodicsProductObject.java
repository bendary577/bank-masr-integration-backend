package com.sun.supplierpoc.models.aggregtor.foodics;

import com.sun.supplierpoc.models.aggregtor.foodics.Option;

import java.util.ArrayList;
import java.util.List;

public class FoodicsProductObject {
    public ArrayList<Option> options;
    public String product_id;
    public int quantity;
    public double unit_price;
    public String kitchen_notes;
    public ArrayList<Tax> taxes;

    private int status;
    private String message;

    public ArrayList<Option> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<Option> options) {
        this.options = options;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
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

    public String getKitchen_notes() {
        return kitchen_notes;
    }

    public void setKitchen_notes(String kitchen_notes) {
        this.kitchen_notes = kitchen_notes;
    }

    public ArrayList<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<Tax> taxes) {
        this.taxes = taxes;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

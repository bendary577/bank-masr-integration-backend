package com.sun.supplierpoc.controllers.PurchasOder;

import java.util.HashMap;
import java.util.List;


public class PorderRequest {

    private  int id;
    private String vendor;
    private String date;
    private String reference;
    private List<String> itemGroups;
    private HashMap<Integer, String> items;
    private HashMap<Integer, String> itemQuantity;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(List<String> itemGroups) {
        this.itemGroups = itemGroups;
    }

    public HashMap<Integer, String> getItems() {
        return items;
    }

    public void setItems(HashMap<Integer, String> items) {
        this.items = items;
    }

    public HashMap<Integer, String> getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(HashMap<Integer, String> itemQuantity) {
        this.itemQuantity = itemQuantity;
    }
}

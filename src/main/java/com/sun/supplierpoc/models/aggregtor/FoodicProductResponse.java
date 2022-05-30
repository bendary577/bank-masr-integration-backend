package com.sun.supplierpoc.models.aggregtor;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;

import java.util.ArrayList;

public class FoodicProductResponse {

    private ArrayList<FoodicsProduct> data;
    private Links links;
    private Meta meta;
    private boolean status;
    private String message;

    public ArrayList<FoodicsProduct> getData() {
        return data;
    }

    public void setData(ArrayList<FoodicsProduct> data) {
        this.data = data;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

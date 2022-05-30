package com.sun.supplierpoc.models.aggregtor;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;

import java.util.ArrayList;

public class FoodicPaginatedProductResponse {

    private ArrayList<FoodicsProduct> data;
    private Links links;
    private Meta meta;

    public ArrayList<FoodicsProduct> getData() {
        return data;
    }

    public void setData(ArrayList<FoodicsProduct> data) {
        this.data = data;
    }
}

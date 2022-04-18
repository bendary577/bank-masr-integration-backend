package com.sun.supplierpoc.models.aggregtor;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;

import java.util.ArrayList;

public class FoodicProductResponse {

    private ArrayList<FoodicsProduct> data;

    public ArrayList<FoodicsProduct> getData() {
        return data;
    }

    public void setData(ArrayList<FoodicsProduct> data) {
        this.data = data;
    }
}

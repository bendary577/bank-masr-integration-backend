package com.sun.supplierpoc.models.talabat;

import com.sun.supplierpoc.models.talabat.foodics.Product;

import java.util.ArrayList;

public class FoodicProductResponse {

    private ArrayList<Product> data;

    public ArrayList<Product> getData() {
        return data;
    }

    public void setData(ArrayList<Product> data) {
        this.data = data;
    }
}

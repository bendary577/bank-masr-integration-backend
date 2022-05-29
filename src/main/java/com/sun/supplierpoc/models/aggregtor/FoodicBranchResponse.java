package com.sun.supplierpoc.models.aggregtor;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsBranch;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;

import java.util.ArrayList;

public class FoodicBranchResponse {

    private ArrayList<FoodicsBranch> data;

    public ArrayList<FoodicsBranch> getData() {
        return data;
    }

    public void setData(ArrayList<FoodicsBranch> data) {
        this.data = data;
    }
}

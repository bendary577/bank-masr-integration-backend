package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class Product {

    private String type;

    private List<FoodicsProduct> foodicsProducts;

    private Date syncDate;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FoodicsProduct> getFoodicsProducts() {
        return foodicsProducts;
    }

    public void setFoodicsProducts(List<FoodicsProduct> foodicsProducts) {
        this.foodicsProducts = foodicsProducts;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }
}

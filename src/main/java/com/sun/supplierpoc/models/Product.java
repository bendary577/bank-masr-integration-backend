package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.simphony.SimphonyMenuItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class Product {

    @Id
    private String id;

    private String type;

    private List<FoodicsProduct> foodicsProducts;

    private List<SimphonyMenuItem> simphonyMenuItems;

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

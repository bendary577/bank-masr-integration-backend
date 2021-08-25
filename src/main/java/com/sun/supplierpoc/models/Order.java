package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.simphony.response.CondimentResponse;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    @Id
    private ObjectId _id = ObjectId.get();
    private String name;
    private int quantity;
    private String size;
    private List<CondimentResponse> toppings;
    private double price;
    private int spicyLevel;
    private String imageUrl;
    private Date creationDate = new Date();
    private boolean deleted = false;

    public Order() {
    }

    public Order(String name, int quantity, String size, List<CondimentResponse> toppings,
                 double price, int spicyLevel, String imageUrl) {
        this.name = name;
        this.quantity = quantity;
        this.size = size;
        this.toppings = toppings;
        this.price = price;
        this.spicyLevel = spicyLevel;
        this.imageUrl = imageUrl;
    }

    public ObjectId getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }



    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<CondimentResponse> getToppings() {
        return toppings;
    }

    public void setToppings(List<CondimentResponse> toppings) {
        this.toppings = toppings;
    }

    public int getSpicyLevel() {
        return spicyLevel;
    }

    public void setSpicyLevel(int spicyLevel) {
        this.spicyLevel = spicyLevel;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.simphony.response.CondimentResponse;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MenuItemMap implements Serializable {
    @Id
    private int id;
    private String firstName;
    private String secondName;
    private String availability;
    private List<CondimentResponse> requiredCondiments;
    private List<CondimentResponse> optionalCondiments;
    private double rating;
    private double smallPrice;
    private double mediumPrice;
    private double largePrice;
    private String imageUrl;
    private Date creationDate = new Date();
    private boolean deleted;
    private String category;
    private String ingredients;

    public MenuItemMap() {
    }

    public MenuItemMap(int id, String firstName, String secondName, String availability, double smallPrice,
                       double mediumPrice, double largePrice, double rating, List<CondimentResponse> requiredCondiments,
                       List<CondimentResponse>optionalCondiments, String imageUrl, boolean deleted,
                       String category, String ingredients) {
        this.id = id;
        this.firstName = firstName;
        this.secondName = secondName;
        this.availability = availability;
        this.requiredCondiments = requiredCondiments;
        this.optionalCondiments = optionalCondiments;
        this.rating = rating;
        this.smallPrice = smallPrice;
        this.mediumPrice = mediumPrice;
        this.largePrice = largePrice;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
        this.category = category;
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public double getSmallPrice() {
        return smallPrice;
    }

    public void setSmallPrice(double smallPrice) {
        this.smallPrice = smallPrice;
    }

    public double getMediumPrice() {
        return mediumPrice;
    }

    public void setMediumPrice(double mediumPrice) {
        this.mediumPrice = mediumPrice;
    }

    public double getLargePrice() {
        return largePrice;
    }

    public void setLargePrice(double largePrice) {
        this.largePrice = largePrice;
    }

    public List<CondimentResponse> getRequiredCondiments() {
        return requiredCondiments;
    }

    public void setRequiredCondiments(List<CondimentResponse> requiredCondiments) {
        this.requiredCondiments = requiredCondiments;
    }

    public List<CondimentResponse> getOptionalCondiments() {
        return optionalCondiments;
    }

    public void setOptionalCondiments(List<CondimentResponse> optionalCondiments) {
        this.optionalCondiments = optionalCondiments;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }
}

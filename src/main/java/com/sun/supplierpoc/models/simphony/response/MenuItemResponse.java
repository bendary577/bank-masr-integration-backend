package com.sun.supplierpoc.models.simphony.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuItemResponse {
    private Long id;
    private String firstName;
    private String secondName;
    private String availability;
    private double price;
    private List<CondimentResponse> requiredCondiments;
    private List<CondimentResponse> optionalCondiments;
    private int rating;
    private double priceMedium;
    private double priceLarge;
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceMedium() {
        return priceMedium;
    }

    public void setPriceMedium(double priceMedium) {
        this.priceMedium = priceMedium;
    }

    public double getPriceLarge() {
        return priceLarge;
    }

    public void setPriceLarge(double priceLarge) {
        this.priceLarge = priceLarge;
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
}

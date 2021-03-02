package com.sun.supplierpoc.models.simphony.response;

import java.util.List;

public class MenuItemResponse {

    private int id;
    private String firstName;
    private String secondName;
    private String availability;
    private double price;
    private List<CondimentResponse> requiredCondiments;
    private List<CondimentResponse> optionalCondiments;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

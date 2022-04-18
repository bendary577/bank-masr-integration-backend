package com.sun.supplierpoc.models.aggregtor.foodics;

public class CreateOrderRequest {

    private FoodicsOrder data;
    private boolean callResponse;
    private String message;

    public FoodicsOrder getData() {
        return data;
    }

    public void setData(FoodicsOrder data) {
        this.data = data;
    }

    public boolean isCallResponse() {
        return callResponse;
    }

    public void setCallResponse(boolean callResponse) {
        this.callResponse = callResponse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

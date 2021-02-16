package com.sun.supplierpoc.models.simphony.response;

import com.sun.supplierpoc.models.OperationData;

import java.util.ArrayList;

public class ZealLoyaltyResponse {

    private int id;
    private String message;
    private String code;
    private boolean loyalty;
    private boolean payment;
    private String status;
    private ArrayList<OperationData> addedOperationData = new ArrayList<>();

    public ZealLoyaltyResponse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isLoyalty() {
        return loyalty;
    }

    public void setLoyalty(boolean loyalty) {
        this.loyalty = loyalty;
    }

    public boolean isPayment() {
        return payment;
    }

    public void setPayment(boolean payment) {
        this.payment = payment;
    }

    public ArrayList<OperationData> getAddedOperationData() {
        return addedOperationData;
    }

    public void setAddedOperationData(ArrayList<OperationData> addedOperationData) {
        this.addedOperationData = addedOperationData;
    }

    @Override
    public String toString() {
        return "ZealLoyaltyResponse{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                ", loyalty=" + loyalty +
                ", payment=" + payment +
                ", addedOperationData=" + addedOperationData +
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

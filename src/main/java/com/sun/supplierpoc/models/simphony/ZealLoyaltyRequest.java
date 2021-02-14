package com.sun.supplierpoc.models.simphony;

public class ZealLoyaltyRequest {

    private int id;
    private String visitedId;
    private double receipt;
    private double receiptNumber;

    public ZealLoyaltyRequest() {
    }

    public ZealLoyaltyRequest(String visitedId, double receipt, double receiptNumber) {
        this.visitedId = visitedId;
        this.receipt = receipt;
        this.receiptNumber = receiptNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVisitedId() {
        return visitedId;
    }

    public void setVisitedId(String visitedId) {
        this.visitedId = visitedId;
    }

    public double getReceipt() {
        return receipt;
    }

    public void setReceipt(double receipt) {
        this.receipt = receipt;
    }

    public double getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(double receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
}

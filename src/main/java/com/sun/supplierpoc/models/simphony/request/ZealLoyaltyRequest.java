package com.sun.supplierpoc.models.simphony.request;

public class ZealLoyaltyRequest {

    private int id;
    private String visitId;
    private double receipt;
    private double receiptNumber;

    public ZealLoyaltyRequest() {
    }

    public ZealLoyaltyRequest(String visitId, double receipt, double receiptNumber) {
        this.visitId = visitId;
        this.receipt = receipt;
        this.receiptNumber = receiptNumber;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

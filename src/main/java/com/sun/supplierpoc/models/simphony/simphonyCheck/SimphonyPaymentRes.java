package com.sun.supplierpoc.models.simphony.simphonyCheck;

public class SimphonyPaymentRes {

    private boolean payment = false;
    private String amount = "0";
    private String tips = "0";
    private String message = "";

    public boolean isPayment() {
        return payment;
    }

    public SimphonyPaymentRes() {
    }
    
    public void setPayment(boolean payment) {
        this.payment = payment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}

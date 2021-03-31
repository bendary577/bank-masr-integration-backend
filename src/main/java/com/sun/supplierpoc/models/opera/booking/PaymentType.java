package com.sun.supplierpoc.models.opera.booking;

public class PaymentType {
    private boolean checked = false;
    private String typeId = "";
    private String paymentType = "";
    private String paymentDescription = "";

    public PaymentType() {}

    public PaymentType(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(String paymentDescription) {
        this.paymentDescription = paymentDescription;
    }
}

package com.sun.supplierpoc.models.opera.booking;

public class PaymentType {
    public boolean checked = false;
    public String typeId = "";
    public String paymentType = "";
    public String paymentDescription = "";

    public PaymentType() {}

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

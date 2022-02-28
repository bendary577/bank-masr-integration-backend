package com.sun.supplierpoc.models.talabat.foodics;

public class Payment {

    private Double amount;
    private Double tendered;
    private String paymentMethodId;
    private Integer tips;
    private Meta meta;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTendered() {
        return tendered;
    }

    public void setTendered(Double tendered) {
        this.tendered = tendered;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Integer getTips() {
        return tips;
    }

    public void setTips(Integer tips) {
        this.tips = tips;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

}

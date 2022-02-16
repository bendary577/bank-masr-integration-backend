package com.sun.supplierpoc.models.talabat.TalabatRest;

public class Payment {

    private Double total;
    private Double discount;
    private String voucher;
    private String minimumOrderValueFee;

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getVoucher() {
        return voucher;
    }

    public void setVoucher(String voucher) {
        this.voucher = voucher;
    }

    public String getMinimumOrderValueFee() {
        return minimumOrderValueFee;
    }

    public void setMinimumOrderValueFee(String minimumOrderValueFee) {
        this.minimumOrderValueFee = minimumOrderValueFee;
    }

}
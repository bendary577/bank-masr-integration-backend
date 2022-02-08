package com.sun.supplierpoc.models.talabat.TalabatRest;

public class Payment {

    private String total;
    private String discount;
    private String voucher;
    private String minimumOrderValueFee;

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
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
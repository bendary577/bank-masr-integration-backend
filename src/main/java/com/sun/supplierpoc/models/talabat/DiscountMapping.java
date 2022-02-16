package com.sun.supplierpoc.models.talabat;

public class DiscountMapping {

    private Long discountId = new Long(0);
    private Double discountRate = new Double(0);

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }
}

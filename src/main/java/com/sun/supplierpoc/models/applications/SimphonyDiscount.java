package com.sun.supplierpoc.models.applications;

public class SimphonyDiscount {
    private float discountRate = 0;
    private int discountId = 0;
    private boolean deleted = false;

    public SimphonyDiscount() { }

    public float getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(float discountRate) {
        this.discountRate = discountRate;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

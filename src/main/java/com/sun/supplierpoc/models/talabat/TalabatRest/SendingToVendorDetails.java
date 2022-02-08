package com.sun.supplierpoc.models.talabat.TalabatRest;

public class SendingToVendorDetails {

    private String estimatedDeliveryTime;
    private String committedPickupTime;
    private String comment;

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public String getCommittedPickupTime() {
        return committedPickupTime;
    }

    public void setCommittedPickupTime(String committedPickupTime) {
        this.committedPickupTime = committedPickupTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
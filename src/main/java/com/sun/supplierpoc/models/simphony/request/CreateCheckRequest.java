package com.sun.supplierpoc.models.simphony.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.supplierpoc.models.simphony.transaction.pGuestCheck;

import java.util.List;

public class CreateCheckRequest {

    @JsonProperty(value = "guestCheck")
    private pGuestCheck pGuestCheck;

    private List<SimphonyMenuItems> simphonyMenuItems;

    public com.sun.supplierpoc.models.simphony.transaction.pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(com.sun.supplierpoc.models.simphony.transaction.pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public List<SimphonyMenuItems> getSimphonyMenuItems() {
        return simphonyMenuItems;
    }

    public void setSimphonyMenuItems(List<SimphonyMenuItems> simphonyMenuItems) {
        this.simphonyMenuItems = simphonyMenuItems;
    }
}

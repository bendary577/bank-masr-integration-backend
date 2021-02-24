package com.sun.supplierpoc.models.simphony.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.supplierpoc.models.simphony.transaction.pGuestCheck;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class CreateCheckRequest {

    @Valid
    @JsonProperty(value = "guestCheck")
    @NotNull(message = "Guest check can't be null.")
    private pGuestCheck pGuestCheck;

    @Valid
    @NotNull(message = "Simphony menu items can't be null.")
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

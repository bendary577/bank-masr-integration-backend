package com.sun.supplierpoc.models.simphony.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CondimentItems {

    @NotBlank(message = "Condiment Id can't be blank.")
    @NotNull(message = "Condiment Id can't be null.")
    private String Id;

    @NotBlank(message = "Condiment quantity can't be blank.")
    @NotNull(message = "Condiment quantity can't be null.")
    @Min(value=1, message = "Quantity must be positive integer value.")
    private String quantity;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

}

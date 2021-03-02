package com.sun.supplierpoc.models.simphony.request;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SimphonyMenuItems {

    @NotBlank(message = "Item Id can't be blank.")
    private String Id;

    @NotBlank(message = "Item quantity can't be blank.")
    @Min(value=1, message = "Quantity must be positive integer value.")
    private String quantity;

    @Valid
    private List<CondimentItems> condimentItems;

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

    public List<CondimentItems> getCondimentItems() {
        return condimentItems;
    }

    public void setCondimentItems(List<CondimentItems> condimentItems) {
        this.condimentItems = condimentItems;
    }
}
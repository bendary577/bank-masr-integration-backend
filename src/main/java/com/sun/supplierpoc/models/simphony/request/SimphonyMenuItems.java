package com.sun.supplierpoc.models.simphony.request;

import java.util.List;

public class SimphonyMenuItems {

    private String Id;
    private String quantity;
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
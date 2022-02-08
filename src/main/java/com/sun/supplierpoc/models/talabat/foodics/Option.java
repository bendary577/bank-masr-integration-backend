package com.sun.supplierpoc.models.talabat.foodics;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Option {

    private String modifierOptionId;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
    private List<Tax> taxes ;

    public String getModifierOptionId() {
        return modifierOptionId;
    }

    public void setModifierOptionId(String modifierOptionId) {
        this.modifierOptionId = modifierOptionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Integer unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<Tax> taxes) {
        this.taxes = taxes;
    }

}


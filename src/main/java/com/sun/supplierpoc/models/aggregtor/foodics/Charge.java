package com.sun.supplierpoc.models.aggregtor.foodics;

import java.util.List;

public class Charge {

    private String chargeId;
    private Integer amount;
    private Integer taxExclusiveAmount;
    private List<Tax> taxes = null;

    public String getChargeId() {
        return chargeId;
    }

    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getTaxExclusiveAmount() {
        return taxExclusiveAmount;
    }

    public void setTaxExclusiveAmount(Integer taxExclusiveAmount) {
        this.taxExclusiveAmount = taxExclusiveAmount;
    }

    public List<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<Tax> taxes) {
        this.taxes = taxes;
    }

}

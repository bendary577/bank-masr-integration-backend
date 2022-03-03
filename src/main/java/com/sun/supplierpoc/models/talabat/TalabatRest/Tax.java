package com.sun.supplierpoc.models.talabat.TalabatRest;

public class Tax{
    public String name;
    public double value;
    public boolean includedInPrice;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isIncludedInPrice() {
        return includedInPrice;
    }

    public void setIncludedInPrice(boolean includedInPrice) {
        this.includedInPrice = includedInPrice;
    }
}

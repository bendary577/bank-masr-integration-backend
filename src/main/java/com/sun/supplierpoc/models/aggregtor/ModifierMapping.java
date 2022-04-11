package com.sun.supplierpoc.models.aggregtor;

public class ModifierMapping {
    private String name;
    private String talabatProductId;
    private String talabatSecProductId;
    private String foodicsProductId;

    public ModifierMapping() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoodicsProductId() {
        return foodicsProductId;
    }

    public void setFoodicsProductId(String foodicsProductId) {
        this.foodicsProductId = foodicsProductId;
    }

    public String getTalabatProductId() {
        return talabatProductId;
    }

    public void setTalabatProductId(String talabatProductId) {
        this.talabatProductId = talabatProductId;
    }

    public String getTalabatSecProductId() {
        return talabatSecProductId;
    }

    public void setTalabatSecProductId(String talabatSecProductId) {
        this.talabatSecProductId = talabatSecProductId;
    }
}

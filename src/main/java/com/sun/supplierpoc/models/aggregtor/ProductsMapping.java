package com.sun.supplierpoc.models.aggregtor;

import java.util.ArrayList;

public class ProductsMapping {

    private String name;
    private String foodIcsProductId;
    private String talabatProductId;
    private ArrayList<ModifierMapping> modifiers = new ArrayList<>();
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoodIcsProductId() {
        return foodIcsProductId;
    }

    public void setFoodIcsProductId(String foodIcsProductId) {
        this.foodIcsProductId = foodIcsProductId;
    }

    public String getTalabatProductId() {
        return talabatProductId;
    }

    public void setTalabatProductId(String talabatProductId) {
        this.talabatProductId = talabatProductId;
    }

    public ArrayList<ModifierMapping> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<ModifierMapping> modifiers) {
        this.modifiers = modifiers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.sun.supplierpoc.models.talabat;

public class ProductsMapping {

    private String name;
    private String foodIcsProductId;
    private String talabatProductId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.sun.supplierpoc.models.aggregtor.foodics;

import java.util.List;

public class Combo {

    private String comboSizeId;
    private Integer quantity;
    private Integer discountType;
    private String discountId;
    private Integer discountAmount;
    private List<FoodicsProduct> foodicsProducts = null;

    public String getComboSizeId() {
        return comboSizeId;
    }

    public void setComboSizeId(String comboSizeId) {
        this.comboSizeId = comboSizeId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public List<FoodicsProduct> getProducts() {
        return foodicsProducts;
    }

    public void setProducts(List<FoodicsProduct> foodicsProducts) {
        this.foodicsProducts = foodicsProducts;
    }

}



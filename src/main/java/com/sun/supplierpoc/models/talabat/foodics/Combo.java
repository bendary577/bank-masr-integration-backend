package com.sun.supplierpoc.models.talabat.foodics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Combo {

    private String comboSizeId;
    private Integer quantity;
    private Integer discountType;
    private String discountId;
    private Integer discountAmount;
    private List<Product> products = null;

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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}



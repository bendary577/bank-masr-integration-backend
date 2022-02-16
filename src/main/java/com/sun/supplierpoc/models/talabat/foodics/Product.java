package com.sun.supplierpoc.models.talabat.foodics;

import java.util.List;

public class Product {

    private String productId;
    private Integer quantity;
    private Double unitPrice;
    private Integer discountAmount;
    private String discountId;
    private Integer discountType;
    private Meta meta;
    private List<Option> options = null;
    private Integer totalPrice;
    private List<Tax> taxes = null;
    private String comboOptionId;
    private String comboSizeId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public Integer getDiscountType() {
        return discountType;
    }

    public void setDiscountType(Integer discountType) {
        this.discountType = discountType;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
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

    public String getComboOptionId() {
        return comboOptionId;
    }

    public void setComboOptionId(String comboOptionId) {
        this.comboOptionId = comboOptionId;
    }

    public String getComboSizeId() {
        return comboSizeId;
    }

    public void setComboSizeId(String comboSizeId) {
        this.comboSizeId = comboSizeId;
    }
}

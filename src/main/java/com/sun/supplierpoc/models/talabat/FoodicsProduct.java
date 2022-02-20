package com.sun.supplierpoc.models.talabat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

public class FoodicsProduct {

    private String id;
    private String sku;
    private String productId;
    private int quantity;
    private Double unitPrice;

    private Object barcode;
    private String name;
    private Object nameLocalized;
    private Object description;
    private Object descriptionLocalized;
    private Object image;
    private Boolean isActive;
    private Boolean isStockProduct;
    private Boolean isReady;
    private Integer pricingMethod;
    private Integer sellingMethod;
    private Integer costingMethod;
    private Object preparationTime;
    private Integer price;
    private Object cost;
    private Object calories;
    private String createdAt;
    private String updatedAt;
    private Object deletedAt;
    private boolean status;
    private String message;
    private List<Object> ingredients = null;

    public List<Object> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Object> ingredients) {
        this.ingredients = ingredients;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Object getBarcode() {
        return barcode;
    }

    public void setBarcode(Object barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getNameLocalized() {
        return nameLocalized;
    }

    public void setNameLocalized(Object nameLocalized) {
        this.nameLocalized = nameLocalized;
    }

    public Object getDescription() {
        return description;
    }

    public void setDescription(Object description) {
        this.description = description;
    }

    public Object getDescriptionLocalized() {
        return descriptionLocalized;
    }

    public void setDescriptionLocalized(Object descriptionLocalized) {
        this.descriptionLocalized = descriptionLocalized;
    }

    public Object getImage() {
        return image;
    }

    public void setImage(Object image) {
        this.image = image;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsStockProduct() {
        return isStockProduct;
    }

    public void setIsStockProduct(Boolean isStockProduct) {
        this.isStockProduct = isStockProduct;
    }

    public Boolean getIsReady() {
        return isReady;
    }

    public void setIsReady(Boolean isReady) {
        this.isReady = isReady;
    }

    public Integer getPricingMethod() {
        return pricingMethod;
    }

    public void setPricingMethod(Integer pricingMethod) {
        this.pricingMethod = pricingMethod;
    }

    public Integer getSellingMethod() {
        return sellingMethod;
    }

    public void setSellingMethod(Integer sellingMethod) {
        this.sellingMethod = sellingMethod;
    }

    public Integer getCostingMethod() {
        return costingMethod;
    }

    public void setCostingMethod(Integer costingMethod) {
        this.costingMethod = costingMethod;
    }

    public Object getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(Object preparationTime) {
        this.preparationTime = preparationTime;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Object getCost() {
        return cost;
    }

    public void setCost(Object cost) {
        this.cost = cost;
    }

    public Object getCalories() {
        return calories;
    }

    public void setCalories(Object calories) {
        this.calories = calories;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Object getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Object deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getStockProduct() {
        return isStockProduct;
    }

    public void setStockProduct(Boolean stockProduct) {
        isStockProduct = stockProduct;
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}

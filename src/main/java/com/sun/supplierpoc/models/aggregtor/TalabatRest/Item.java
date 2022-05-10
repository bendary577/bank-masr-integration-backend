package com.sun.supplierpoc.models.aggregtor.TalabatRest;

import com.sun.supplierpoc.models.aggregtor.ModifierMapping;

import java.util.ArrayList;
import java.util.List;

public class Item {

    private String id;
    private String name;
    private Integer quantity;
    private String unitPrice;
    private String parentName;
    private List<Object> options = null;
    public int amount;
    public String category;
    public String menuNumber;
    public String comment;
    public double price;
    public double total;
    public ArrayList<Modifier> modifiers;
    public String productId;
    public boolean modifiable;
    public String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public List<Object> getOptions() {
        return options;
    }

    public void setOptions(List<Object> options) {
        this.options = options;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMenuNumber() {
        return menuNumber;
    }

    public void setMenuNumber(String menuNumber) {
        this.menuNumber = menuNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public ArrayList<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean checkModifierExistence(ArrayList<Modifier> modifiers, String modifierTalabatId){
        for (Modifier modifierMapping : modifiers) {
            if(modifierMapping.getProductId().equals(modifierTalabatId))
                return true;
        }
        return false;
    }

    public Modifier checkModifierExistenceAndGet(ArrayList<Modifier> modifiers, String modifierTalabatId){
        for (Modifier modifierMapping : modifiers) {
            if(modifierMapping.getProductId().equals(modifierTalabatId)){
                return modifierMapping;
            }
        }
        return null;
    }


}

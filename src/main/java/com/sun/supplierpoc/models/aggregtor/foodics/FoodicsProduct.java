package com.sun.supplierpoc.models.aggregtor.foodics;

import java.util.ArrayList;

public class FoodicsProduct {

    public String id;
    public String sku;
    public String barcode;
    public String name;
    public Object name_localized;
    public String description;
    public Object description_localized;
    public String image;
    public boolean is_active;
    public boolean is_stock_product;
    public boolean is_ready;
    public int pricing_method;
    public int selling_method;
    public int costing_method;
    public Object preparation_time;
    public double price;
    public Object cost;
    public int calories;
    public String created_at;
    public String updated_at;
    public Object deleted_at;
    public ArrayList<Discount> discounts;
    public ArrayList<TimedEvent> timed_events;
    public Category category;
    public TaxGroup tax_group;
    public ArrayList<Tag> tags;
    public ArrayList<Group> groups;
    public ArrayList<Branch> branches;
    public ArrayList<Modifier> modifiers;
    public ArrayList<Ingredient> ingredients;

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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getName_localized() {
        return name_localized;
    }

    public void setName_localized(Object name_localized) {
        this.name_localized = name_localized;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDescription_localized() {
        return description_localized;
    }

    public void setDescription_localized(Object description_localized) {
        this.description_localized = description_localized;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean isIs_stock_product() {
        return is_stock_product;
    }

    public void setIs_stock_product(boolean is_stock_product) {
        this.is_stock_product = is_stock_product;
    }

    public boolean isIs_ready() {
        return is_ready;
    }

    public void setIs_ready(boolean is_ready) {
        this.is_ready = is_ready;
    }

    public int getPricing_method() {
        return pricing_method;
    }

    public void setPricing_method(int pricing_method) {
        this.pricing_method = pricing_method;
    }

    public int getSelling_method() {
        return selling_method;
    }

    public void setSelling_method(int selling_method) {
        this.selling_method = selling_method;
    }

    public int getCosting_method() {
        return costing_method;
    }

    public void setCosting_method(int costing_method) {
        this.costing_method = costing_method;
    }

    public Object getPreparation_time() {
        return preparation_time;
    }

    public void setPreparation_time(Object preparation_time) {
        this.preparation_time = preparation_time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Object getCost() {
        return cost;
    }

    public void setCost(Object cost) {
        this.cost = cost;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public Object getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(Object deleted_at) {
        this.deleted_at = deleted_at;
    }

    public ArrayList<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(ArrayList<Discount> discounts) {
        this.discounts = discounts;
    }

    public ArrayList<TimedEvent> getTimed_events() {
        return timed_events;
    }

    public void setTimed_events(ArrayList<TimedEvent> timed_events) {
        this.timed_events = timed_events;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TaxGroup getTax_group() {
        return tax_group;
    }

    public void setTax_group(TaxGroup tax_group) {
        this.tax_group = tax_group;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    public ArrayList<Branch> getBranches() {
        return branches;
    }

    public void setBranches(ArrayList<Branch> branches) {
        this.branches = branches;
    }

    public ArrayList<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(ArrayList<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }


}

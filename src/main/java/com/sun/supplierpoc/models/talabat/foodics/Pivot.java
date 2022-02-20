package com.sun.supplierpoc.models.talabat.foodics;

import java.util.ArrayList;

public class Pivot{

    public String product_id;
    public String discount_id;
    public String timed_event_id;
    public String tag_id;
    public int price;
    public boolean is_active;
    public boolean is_in_stock;
    public boolean is_splittable_in_half;
    public boolean unique_options;
    public int minimum_options;
    public int maximum_options;
    public int free_options;
    public ArrayList<String> default_options_ids;
    public ArrayList<String> excluded_options_ids;
    public int quantity;
    public ArrayList inactive_in_order_types;

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getDiscount_id() {
        return discount_id;
    }

    public void setDiscount_id(String discount_id) {
        this.discount_id = discount_id;
    }

    public String getTimed_event_id() {
        return timed_event_id;
    }

    public void setTimed_event_id(String timed_event_id) {
        this.timed_event_id = timed_event_id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean isIs_in_stock() {
        return is_in_stock;
    }

    public void setIs_in_stock(boolean is_in_stock) {
        this.is_in_stock = is_in_stock;
    }

    public boolean isIs_splittable_in_half() {
        return is_splittable_in_half;
    }

    public void setIs_splittable_in_half(boolean is_splittable_in_half) {
        this.is_splittable_in_half = is_splittable_in_half;
    }

    public boolean isUnique_options() {
        return unique_options;
    }

    public void setUnique_options(boolean unique_options) {
        this.unique_options = unique_options;
    }

    public int getMinimum_options() {
        return minimum_options;
    }

    public void setMinimum_options(int minimum_options) {
        this.minimum_options = minimum_options;
    }

    public int getMaximum_options() {
        return maximum_options;
    }

    public void setMaximum_options(int maximum_options) {
        this.maximum_options = maximum_options;
    }

    public int getFree_options() {
        return free_options;
    }

    public void setFree_options(int free_options) {
        this.free_options = free_options;
    }

    public ArrayList<String> getDefault_options_ids() {
        return default_options_ids;
    }

    public void setDefault_options_ids(ArrayList<String> default_options_ids) {
        this.default_options_ids = default_options_ids;
    }

    public ArrayList<String> getExcluded_options_ids() {
        return excluded_options_ids;
    }

    public void setExcluded_options_ids(ArrayList<String> excluded_options_ids) {
        this.excluded_options_ids = excluded_options_ids;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public ArrayList getInactive_in_order_types() {
        return inactive_in_order_types;
    }

    public void setInactive_in_order_types(ArrayList inactive_in_order_types) {
        this.inactive_in_order_types = inactive_in_order_types;
    }
}

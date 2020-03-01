package com.sun.supplierpoc;

import com.sun.supplierpoc.models.SyncJobData;

import java.util.ArrayList;
import java.util.HashMap;

public class Conversions {
    public Conversions() {
    }

    public String transformColName(String columnName){
        return columnName.strip().toLowerCase().replace(' ', '_');
    }
    public HashMap<String, Object> checkOverGroupExistence(ArrayList<HashMap<String, String>> overGroups, String overGroupName) {
        HashMap<String, Object> data = new HashMap<>();
        for (HashMap<String, String> overGroup : overGroups) {
            if (overGroup.get("over_group").equals(overGroupName)) {
                data.put("status", true);
                data.put("overGroup", overGroup);
                return data;
            }
        }
        data.put("status", false);
        data.put("overGroup", new HashMap<String, String>());
        return data;
    }

    public HashMap<String, Object> checkMajorGroupExistence(ArrayList<HashMap<String, String>> majorGroups, String majorGroupName){
        HashMap<String, Object> data = new HashMap<>();
        for (HashMap<String, String> majorGroup : majorGroups) {
            if (majorGroup.get("major_group").equals(majorGroupName)) {
                data.put("status", true);
                data.put("majorGroup", majorGroup);
                return data;
            }
        }
        data.put("status", false);
        data.put("majorGroup", new HashMap<String, String>());
        return data;
    }

    public HashMap<String, Object> checkItemGroupExistence(ArrayList<HashMap<String, String>> itemGroups, String itemGroupName){
        HashMap<String, Object> data = new HashMap<>();
        for (HashMap<String, String> itemGroup : itemGroups) {
            if (itemGroup.get("item_group").equals(itemGroupName)) {
                data.put("status", true);
                data.put("itemGroup", itemGroup);
                return data;
            }
        }
        data.put("status", false);
        data.put("itemGroup", new HashMap<String, String>());
        return data;
    }

    public HashMap<String, Object> checkItemExistence(ArrayList<HashMap<String, String>> items, String itemName){
        HashMap<String, Object> data = new HashMap<>();
        for (HashMap<String, String> item : items) {
            if (item.get("item").equals(itemName)) {
                data.put("status", true);
                data.put("item", item);
                return data;
            }
        }
        data.put("status", false);
        data.put("item", new HashMap<String, String>());
        return data;
    }

    public HashMap<String, Object> checkSupplierExistence(ArrayList<SyncJobData> suppliers, String vendorName){
        HashMap<String, Object> data = new HashMap<>();
        for (SyncJobData supplier : suppliers) {
            if (supplier.getData().get("supplier").equals(vendorName)) {
                data.put("status", true);
                data.put("supplier", supplier);
                return data;
            }
        }
        data.put("status", false);
        data.put("supplier", new SyncJobData());
        return data;
    }

    public float convertStringToFloat(String value){
        value = value.toLowerCase().replaceAll(",", "").replace("(", "").replace(")", "");
        return Float.parseFloat(value);
    }
}

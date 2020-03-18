package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;

public class BasicConfiguration implements Serializable {
    @Id
    private String id;
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenters = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<ItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(ArrayList<ItemGroup> itemGroups) {
        this.itemGroups = itemGroups;
    }

    public ArrayList<OverGroup> getOverGroups() {
        return overGroups;
    }

    public void setOverGroups(ArrayList<OverGroup> overGroups) {
        this.overGroups = overGroups;
    }

    public ArrayList<MajorGroup> getMajorGroups() {
        return majorGroups;
    }

    public void setMajorGroups(ArrayList<MajorGroup> majorGroups) {
        this.majorGroups = majorGroups;
    }

    public ArrayList<CostCenter> getCostCenters() {
        return costCenters;
    }

    public void setCostCenters(ArrayList<CostCenter> costCenters) {
        this.costCenters = costCenters;
    }
}

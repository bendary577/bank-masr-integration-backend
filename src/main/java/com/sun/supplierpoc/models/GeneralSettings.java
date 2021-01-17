package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class GeneralSettings {
    @Id
    private String id;
    private String accountId;
    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    private ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenterAccountMapping = new ArrayList<>();
    private ArrayList<CostCenter> locations = new ArrayList<>();
    private ArrayList<RevenueCenter> revenueCenters = new ArrayList<>();
    // Menu Items variables
    private ArrayList<SimphonyLocation> simphonyLocations = new ArrayList<>();
    private Date creationDate;
    private boolean deleted;

    public GeneralSettings() {
    }

    public GeneralSettings(String accountId, Date creationDate) {
        this.accountId = accountId;
        this.creationDate = creationDate;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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


    public ArrayList<MajorGroup> getMajorGroups() {
        return majorGroups;
    }

    public void setMajorGroups(ArrayList<MajorGroup> majorGroups) {
        this.majorGroups = majorGroups;
    }

    public ArrayList<OverGroup> getOverGroups() {
        return overGroups;
    }

    public void setOverGroups(ArrayList<OverGroup> overGroups) {
        this.overGroups = overGroups;
    }

    public ArrayList<CostCenter> getCostCenterAccountMapping() {
        return costCenterAccountMapping;
    }

    public void setCostCenterAccountMapping(ArrayList<CostCenter> costCenterAccountMapping) {
        this.costCenterAccountMapping = costCenterAccountMapping;
    }

    public ArrayList<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(ArrayList<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }

    public ArrayList<CostCenter> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<CostCenter> locations) {
        this.locations = locations;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ArrayList<SimphonyLocation> getSimphonyLocations() {
        return simphonyLocations;
    }

    public void setSimphonyLocations(ArrayList<SimphonyLocation> simphonyLocations) {
        this.simphonyLocations = simphonyLocations;
    }

    public SimphonyLocation getSimphonyLocationsByID(int revenueCenterID){
        for (SimphonyLocation location : this.simphonyLocations) {
            if (location.getRevenueCenterID() == revenueCenterID) {
                return location;
            }
        }
        return null;
    }
}

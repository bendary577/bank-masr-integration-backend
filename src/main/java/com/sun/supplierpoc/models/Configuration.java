package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;


public class Configuration implements Serializable {
    @Id
    private String id;
    private String businessUnit = "";
    private String journalSource = "";
    private String transactionReference = "";
    private String journalType = "";
    private String currencyCode = "";
    private String postingType = "";
    private String timePeriod = "";
    private String taxes = "";
    private String groups = "";

    private String hour = "";
    private String duration = "";
    private String day = "";
    private String dayName = "";

    private String inventoryAccount = "";
    private String expensesAccount = "";
    private String locationAnalysis = "";

    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenters = new ArrayList<>();
    private ArrayList<CostCenter> costCenterLocationMapping = new ArrayList<>();
    private ArrayList<WasteGroup> wasteGroups = new ArrayList<>();
    private ArrayList<Analysis> analysis = new ArrayList<>();

    private AccountSettings accountSettings;

    public Configuration() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getJournalSource() {
        return journalSource;
    }

    public void setJournalSource(String journalSource) {
        this.journalSource = journalSource;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getJournalType() {
        return journalType;
    }

    public void setJournalType(String journalType) {
        this.journalType = journalType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPostingType() {
        return postingType;
    }

    public void setPostingType(String postingType) {
        this.postingType = postingType;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getInventoryAccount() {
        return inventoryAccount;
    }

    public void setInventoryAccount(String inventoryAccount) {
        this.inventoryAccount = inventoryAccount;
    }

    public String getExpensesAccount() {
        return expensesAccount;
    }

    public void setExpensesAccount(String expensesAccount) {
        this.expensesAccount = expensesAccount;
    }

    public String getLocationAnalysis() {
        return locationAnalysis;
    }

    public void setLocationAnalysis(String locationAnalysis) {
        this.locationAnalysis = locationAnalysis;
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

    public ArrayList<CostCenter> getCostCenterLocationMapping() {
        return costCenterLocationMapping;
    }

    public void setCostCenterLocationMapping(ArrayList<CostCenter> costCenterLocationMapping) {
        this.costCenterLocationMapping = costCenterLocationMapping;
    }

    public ArrayList<WasteGroup> getWasteGroups() {
        return wasteGroups;
    }

    public void setWasteGroups(ArrayList<WasteGroup> wasteGroups) {
        this.wasteGroups = wasteGroups;
    }

    public ArrayList<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(ArrayList<Analysis> analysis) {
        this.analysis = analysis;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }
}

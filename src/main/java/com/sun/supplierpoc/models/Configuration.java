package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;


public class Configuration implements Serializable {
    @Id
    private String id;
    private String businessUnit = "";
    private String transactionReference = "";
    private String journalType = "";
    private String currencyCode = "";
    private String postingType = "";
    private String timePeriod = "";
    private String fromDate = "";
    private String toDate = "";
    private String vendorTaxes = "";
    private String groups = "";
    private String suspenseAccount = "";

    private String hour = "";
    private String duration = "";
    private String day = "";
    private String dayName = "";

    private String inventoryAccount = "";
    private String expensesAccount = "";
    private String locationAnalysis = "";

    // Invoices/Credit Notes variables
    private String invoiceTypeIncluded = "";

    // Consumption variables

    /*
    *  get consumption based of (Location/Cost Center)
    * */
    private String consumptionBasedOnType = "";

    // Sales variables
    private String revenue = "";
    private String vatOut = "";
    private String cashShortagePOS = "";
    private String cashSurplusPOS = "";
    private String grossDiscountSales = "";

    private String recordType = "L";
    private String conversionCode = "1";
    private String conversionRate = "1.0";
    private String versionCode = "42601";

    private Boolean uniqueOverGroupMapping = false;
    private Boolean uniqueAnalysisMapping = false;

    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenters = new ArrayList<>();
    private ArrayList<CostCenter> costCenterLocationMapping = new ArrayList<>();
    private ArrayList<WasteGroup> wasteGroups = new ArrayList<>();
    private ArrayList<Analysis> analysis = new ArrayList<>();
    private ArrayList<Tender> tenders = new ArrayList<>();
    private ArrayList<Tax> taxes = new ArrayList<>();
    private ArrayList<Discount> discounts = new ArrayList<>();
    private ArrayList<RevenueCenter> revenueCenters = new ArrayList<>();
    private ArrayList<ServiceCharge> serviceCharges = new ArrayList<>();

    private AccountSettings accountSettings;

    public Configuration() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessUnit() {
        return businessUnit.strip();
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getTransactionReference() {
        return transactionReference.strip();
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getJournalType() {
        return journalType.strip();
    }

    public void setJournalType(String journalType) {
        this.journalType = journalType;
    }

    public String getCurrencyCode() {
        return currencyCode.strip();
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPostingType() {
        return postingType.strip();
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

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getVendorTaxes() {
        return vendorTaxes;
    }

    public void setVendorTaxes(String vendorTaxes) {
        this.vendorTaxes = vendorTaxes;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getSuspenseAccount() {
        return suspenseAccount;
    }

    public void setSuspenseAccount(String suspenseAccount) {
        this.suspenseAccount = suspenseAccount;
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
        return expensesAccount.strip();
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

    public String getInvoiceTypeIncluded() {
        return invoiceTypeIncluded;
    }

    public void setInvoiceTypeIncluded(String invoiceTypeIncluded) {
        this.invoiceTypeIncluded = invoiceTypeIncluded;
    }

    public String getConsumptionBasedOnType() {
        return consumptionBasedOnType;
    }

    public void setConsumptionBasedOnType(String consumptionBasedOnType) {
        this.consumptionBasedOnType = consumptionBasedOnType;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public String getVatOut() {
        return vatOut;
    }

    public void setVatOut(String vatOut) {
        this.vatOut = vatOut;
    }

    public String getCashShortagePOS() {
        return cashShortagePOS;
    }

    public void setCashShortagePOS(String cashShortagePOS) {
        this.cashShortagePOS = cashShortagePOS;
    }

    public String getCashSurplusPOS() {
        return cashSurplusPOS;
    }

    public void setCashSurplusPOS(String cashSurplusPOS) {
        this.cashSurplusPOS = cashSurplusPOS;
    }

    public String getGrossDiscountSales() {
        return grossDiscountSales;
    }

    public void setGrossDiscountSales(String grossDiscountSales) {
        this.grossDiscountSales = grossDiscountSales;
    }

    public Boolean getUniqueOverGroupMapping() {
        return uniqueOverGroupMapping;
    }

    public Boolean getUniqueAnalysisMapping() {
        return uniqueAnalysisMapping;
    }

    public void setUniqueAnalysisMapping(Boolean uniqueAnalysisMapping) {
        this.uniqueAnalysisMapping = uniqueAnalysisMapping;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getConversionCode() {
        return conversionCode;
    }

    public void setConversionCode(String conversionCode) {
        this.conversionCode = conversionCode;
    }

    public String getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(String conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setUniqueOverGroupMapping(Boolean uniqueOverGroupMapping) {
        this.uniqueOverGroupMapping = uniqueOverGroupMapping;
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

    public ArrayList<Tender> getTenders() {
        return tenders;
    }

    public void setTenders(ArrayList<Tender> tenders) {
        this.tenders = tenders;
    }

    public ArrayList<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<Tax> taxes) {
        this.taxes = taxes;
    }

    public ArrayList<Discount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(ArrayList<Discount> discounts) {
        this.discounts = discounts;
    }

    public ArrayList<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(ArrayList<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }

    public ArrayList<ServiceCharge> getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(ArrayList<ServiceCharge> serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }
}

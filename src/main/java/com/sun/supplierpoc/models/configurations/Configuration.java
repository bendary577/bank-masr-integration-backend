package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;


public class Configuration implements Serializable {
    public boolean singleFilePerDay = false;
    public String timePeriod = "";
    public String fromDate = "";
    public String toDate = "";

    public String transactionReference = "";
    public Boolean uniqueOverGroupMapping = false;

    // Exported File variables
    public String recordType = "";
    public String conversionCode = "";
    public String conversionRate = "";
    public String versionCode = "";
    public boolean exportFilePerLocation = false;

    public String inventoryAccount = "";
    public String expensesAccount = "";


    public String locationAnalysisCode = "";
    public String branchAnalysisCode = "";
    public String tenderAnalysisCode = "";
    public String familyGroupAnalysisCode = "";
    public String supplierCodeAnalysisCode = "";
    public String taxesCodeAnalysisCode = "";


    public ArrayList<Analysis> analysis = new ArrayList<>();


    // Sync per (overGroup/itemGroup)
    public String syncPerGroup = "";

    public SupplierConfiguration supplierConfiguration = new SupplierConfiguration();


    public boolean isSingleFilePerDay() {
        return singleFilePerDay;
    }

    public void setSingleFilePerDay(boolean singleFilePerDay) {
        this.singleFilePerDay = singleFilePerDay;
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

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Boolean getUniqueOverGroupMapping() {
        return uniqueOverGroupMapping;
    }

    public void setUniqueOverGroupMapping(Boolean uniqueOverGroupMapping) {
        this.uniqueOverGroupMapping = uniqueOverGroupMapping;
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

    public boolean isExportFilePerLocation() {
        return exportFilePerLocation;
    }

    public void setExportFilePerLocation(boolean exportFilePerLocation) {
        this.exportFilePerLocation = exportFilePerLocation;
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

    public String getLocationAnalysisCode() {
        return locationAnalysisCode;
    }

    public void setLocationAnalysisCode(String locationAnalysisCode) {
        this.locationAnalysisCode = locationAnalysisCode;
    }

    public String getTenderAnalysisCode() {
        return tenderAnalysisCode;
    }

    public void setTenderAnalysisCode(String tenderAnalysisCode) {
        this.tenderAnalysisCode = tenderAnalysisCode;
    }

    public String getFamilyGroupAnalysisCode() {
        return familyGroupAnalysisCode;
    }

    public void setFamilyGroupAnalysisCode(String familyGroupAnalysisCode) {
        this.familyGroupAnalysisCode = familyGroupAnalysisCode;
    }

    public String getSupplierCodeAnalysisCode() {
        return supplierCodeAnalysisCode;
    }

    public void setSupplierCodeAnalysisCode(String supplierCodeAnalysisCode) {
        this.supplierCodeAnalysisCode = supplierCodeAnalysisCode;
    }

    public String getTaxesCodeAnalysisCode() {
        return taxesCodeAnalysisCode;
    }

    public void setTaxesCodeAnalysisCode(String taxesCodeAnalysisCode) {
        this.taxesCodeAnalysisCode = taxesCodeAnalysisCode;
    }

    public ArrayList<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(ArrayList<Analysis> analysis) {
        this.analysis = analysis;
    }


    public SupplierConfiguration getSupplierConfiguration() {
        return supplierConfiguration;
    }

    public void setSupplierConfiguration(SupplierConfiguration supplierConfiguration) {
        this.supplierConfiguration = supplierConfiguration;
    }

}

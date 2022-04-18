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

    public ArrayList<CostCenter> costCenters = new ArrayList<>();

    public String locationAnalysisCode = "";
    public String branchAnalysisCode = "";
    public String tenderAnalysisCode = "";
    public String familyGroupAnalysisCode = "";
    public String supplierCodeAnalysisCode = "";
    public String taxesCodeAnalysisCode = "";


    public ArrayList<Analysis> analysis = new ArrayList<>();

    public ArrayList<Item> items = new ArrayList<>();
    public ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    public ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    public ArrayList<OverGroup> overGroups = new ArrayList<>();

    // Sync per (overGroup/itemGroup)
    public String syncPerGroup = "";

    public SchedulerConfiguration schedulerConfiguration = new SchedulerConfiguration();
    public InforConfiguration inforConfiguration = new InforConfiguration();

    public SalesConfiguration salesConfiguration = new SalesConfiguration();
    public SalesAPIConfig salesAPIConfig = new SalesAPIConfig();
    public SupplierConfiguration supplierConfiguration = new SupplierConfiguration();
    public InvoiceConfiguration invoiceConfiguration = new InvoiceConfiguration();
    public WastageConfiguration wastageConfiguration = new WastageConfiguration();
    public ConsumptionConfiguration consumptionConfiguration = new ConsumptionConfiguration();
    public TransferConfiguration transferConfiguration;
    public MenuItemConfiguration menuItemConfiguration = new MenuItemConfiguration();
    public BookingConfiguration bookingConfiguration = new BookingConfiguration();

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

    public ArrayList<CostCenter> getCostCenters() {
        return costCenters;
    }

    public void setCostCenters(ArrayList<CostCenter> costCenters) {
        this.costCenters = costCenters;
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

    public String getSyncPerGroup() {
        return syncPerGroup;
    }

    public void setSyncPerGroup(String syncPerGroup) {
        this.syncPerGroup = syncPerGroup;
    }

    public SchedulerConfiguration getSchedulerConfiguration() {
        return schedulerConfiguration;
    }

    public void setSchedulerConfiguration(SchedulerConfiguration schedulerConfiguration) {
        this.schedulerConfiguration = schedulerConfiguration;
    }

    public InforConfiguration getInforConfiguration() {
        return inforConfiguration;
    }

    public void setInforConfiguration(InforConfiguration inforConfiguration) {
        this.inforConfiguration = inforConfiguration;
    }

    public SalesConfiguration getSalesConfiguration() {
        return salesConfiguration;
    }

    public void setSalesConfiguration(SalesConfiguration salesConfiguration) {
        this.salesConfiguration = salesConfiguration;
    }

    public SupplierConfiguration getSupplierConfiguration() {
        return supplierConfiguration;
    }

    public void setSupplierConfiguration(SupplierConfiguration supplierConfiguration) {
        this.supplierConfiguration = supplierConfiguration;
    }

    public InvoiceConfiguration getInvoiceConfiguration() {
        return invoiceConfiguration;
    }

    public void setInvoiceConfiguration(InvoiceConfiguration invoiceConfiguration) {
        this.invoiceConfiguration = invoiceConfiguration;
    }

    public WastageConfiguration getWastageConfiguration() {
        return wastageConfiguration;
    }

    public void setWastageConfiguration(WastageConfiguration wastageConfiguration) {
        this.wastageConfiguration = wastageConfiguration;
    }

    public ConsumptionConfiguration getConsumptionConfiguration() {
        return consumptionConfiguration;
    }

    public void setConsumptionConfiguration(ConsumptionConfiguration consumptionConfiguration) {
        this.consumptionConfiguration = consumptionConfiguration;
    }

    public TransferConfiguration getTransferConfiguration() {
        return transferConfiguration;
    }

    public void setTransferConfiguration(TransferConfiguration transferConfiguration) {
        this.transferConfiguration = transferConfiguration;
    }

    public MenuItemConfiguration getMenuItemConfiguration() {
        return menuItemConfiguration;
    }

    public void setMenuItemConfiguration(MenuItemConfiguration menuItemConfiguration) {
        this.menuItemConfiguration = menuItemConfiguration;
    }

    public BookingConfiguration getBookingConfiguration() {
        return bookingConfiguration;
    }

    public void setBookingConfiguration(BookingConfiguration bookingConfiguration) {
        this.bookingConfiguration = bookingConfiguration;
    }

    public SalesAPIConfig getSalesAPIConfig() {
        return salesAPIConfig;
    }

    public void setSalesAPIConfig(SalesAPIConfig salesAPIConfig) {
        this.salesAPIConfig = salesAPIConfig;
    }
}

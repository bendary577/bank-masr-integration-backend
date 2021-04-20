package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;


public class Configuration implements Serializable {
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
    public SupplierConfiguration supplierConfiguration = new SupplierConfiguration();
    public InvoiceConfiguration invoiceConfiguration = new InvoiceConfiguration();
    public WastageConfiguration wastageConfiguration = new WastageConfiguration();
    public ConsumptionConfiguration consumptionConfiguration = new ConsumptionConfiguration();
    public TransferConfiguration transferConfiguration;
    public MenuItemConfiguration menuItemConfiguration = new MenuItemConfiguration();
    public BookingConfiguration bookingConfiguration = new BookingConfiguration();
}

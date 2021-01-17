package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;


public class Configuration implements Serializable {
    public String timePeriod = "";
    public String fromDate = "";
    public String toDate = "";

    public String transactionReference = "";
    public Boolean uniqueOverGroupMapping = false;
    public Boolean uniqueAnalysisMapping = false;

    // Exported File variables
    public String recordType = "";
    public String conversionCode = "";
    public String conversionRate = "";
    public String versionCode = "";
    public boolean exportFilePerLocation = false;

    public String inventoryAccount = "";
    public String expensesAccount = "";

    public ArrayList<CostCenter> costCenters = new ArrayList<>();

    public String locationAnalysis = "";
    public ArrayList<Analysis> analysis = new ArrayList<>();

    public ArrayList<Item> items = new ArrayList<>();
    public ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    public ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    public ArrayList<OverGroup> overGroups = new ArrayList<>();

    public SchedulerConfiguration schedulerConfiguration = new SchedulerConfiguration();
    public InforConfiguration inforConfiguration;

    public SalesConfiguration salesConfiguration;
    public SupplierConfiguration supplierConfiguration;
    public InvoiceConfiguration invoiceConfiguration;
    public WastageConfiguration wastageConfiguration;
    public ConsumptionConfiguration consumptionConfiguration;
    public TransferConfiguration transferConfiguration;
    public MenuItemConfiguration menuItemConfiguration;
}

package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;

import java.util.ArrayList;

public class JournalBatch {
    private CostCenter costCenter = new CostCenter();
    private double salesDifferent = 0.0;
    private ArrayList<Tax> salesTax = new ArrayList<>();
    private ArrayList<Tender> salesTender = new ArrayList<>();
    private ArrayList<Journal> salesMajorGroupGross = new ArrayList<>();

    private SyncJobData salesDifferentData = new SyncJobData();
    private ArrayList<SyncJobData> salesTaxData = new ArrayList<>();
    private ArrayList<SyncJobData> salesTenderData = new ArrayList<>();
    private ArrayList<SyncJobData> salesMajorGroupGrossData = new ArrayList<>();

    public JournalBatch() {}

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public double getSalesDifferent() {
        return salesDifferent;
    }

    public void setSalesDifferent(double salesDifferent) {
        this.salesDifferent = salesDifferent;
    }

    public ArrayList<Tax> getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(ArrayList<Tax> salesTax) {
        this.salesTax = salesTax;
    }

    public ArrayList<Tender> getSalesTender() {
        return salesTender;
    }

    public void setSalesTender(ArrayList<Tender> salesTender) {
        this.salesTender = salesTender;
    }

    public ArrayList<Journal> getSalesMajorGroupGross() {
        return salesMajorGroupGross;
    }

    public void setSalesMajorGroupGross(ArrayList<Journal> salesMajorGroupGross) {
        this.salesMajorGroupGross = salesMajorGroupGross;
    }

    public SyncJobData getSalesDifferentData() {
        return salesDifferentData;
    }

    public void setSalesDifferentData(SyncJobData salesDifferentData) {
        this.salesDifferentData = salesDifferentData;
    }

    public ArrayList<SyncJobData> getSalesTaxData() {
        return salesTaxData;
    }

    public void setSalesTaxData(ArrayList<SyncJobData> salesTaxData) {
        this.salesTaxData = salesTaxData;
    }

    public ArrayList<SyncJobData> getSalesTenderData() {
        return salesTenderData;
    }

    public void setSalesTenderData(ArrayList<SyncJobData> salesTenderData) {
        this.salesTenderData = salesTenderData;
    }

    public ArrayList<SyncJobData> getSalesMajorGroupGrossData() {
        return salesMajorGroupGrossData;
    }

    public void setSalesMajorGroupGrossData(ArrayList<SyncJobData> salesMajorGroupGrossData) {
        this.salesMajorGroupGrossData = salesMajorGroupGrossData;
    }
}

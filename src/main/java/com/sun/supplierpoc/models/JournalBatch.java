package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;

import java.util.ArrayList;
import java.util.HashMap;

public class JournalBatch {
    private CostCenter costCenter = new CostCenter();
    private double salesDifferent = 0.0;
    private SalesStatistics salesStatistics = new SalesStatistics();

    private ArrayList<Tax> salesTax = new ArrayList<>();
    private ArrayList<Tender> salesTender = new ArrayList<>();
    private ArrayList<Discount> salesDiscount= new ArrayList<>();
    private ArrayList<Journal> salesMajorGroupGross = new ArrayList<>();
    private ArrayList<ServiceCharge> salesServiceCharge = new ArrayList<>();
    private ArrayList<HashMap<String, String>> waste = new ArrayList<>();

    private SyncJobData salesDifferentData = new SyncJobData();
    private ArrayList<SyncJobData> salesTaxData = new ArrayList<>();
    private ArrayList<SyncJobData> salesTenderData = new ArrayList<>();
    private ArrayList<SyncJobData> salesDiscountData = new ArrayList<>();
    private ArrayList<SyncJobData> salesMajorGroupGrossData = new ArrayList<>();
    private ArrayList<SyncJobData> salesServiceChargeData = new ArrayList<>();
    private ArrayList<SyncJobData> statisticsData = new ArrayList<>();
    private ArrayList<SyncJobData> wasteData = new ArrayList<>();

    public JournalBatch() {}

    public JournalBatch(CostCenter costCenter, ArrayList<HashMap<String, String>> waste) {
        this.costCenter = costCenter;
        this.waste = waste;
    }

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

    public ArrayList<Discount> getSalesDiscount() {
        return salesDiscount;
    }

    public void setSalesDiscount(ArrayList<Discount> salesDiscount) {
        this.salesDiscount = salesDiscount;
    }

    public ArrayList<SyncJobData> getSalesDiscountData() {
        return salesDiscountData;
    }

    public void setSalesDiscountData(ArrayList<SyncJobData> salesDiscountData) {
        this.salesDiscountData = salesDiscountData;
    }

    public ArrayList<ServiceCharge> getSalesServiceCharge() {
        return salesServiceCharge;
    }

    public void setSalesServiceCharge(ArrayList<ServiceCharge> salesServiceCharge) {
        this.salesServiceCharge = salesServiceCharge;
    }

    public ArrayList<SyncJobData> getSalesServiceChargeData() {
        return salesServiceChargeData;
    }

    public void setSalesServiceChargeData(ArrayList<SyncJobData> salesServiceChargeData) {
        this.salesServiceChargeData = salesServiceChargeData;
    }

    public SalesStatistics getSalesStatistics() {
        return salesStatistics;
    }

    public void setSalesStatistics(SalesStatistics salesStatistics) {
        this.salesStatistics = salesStatistics;
    }

    public ArrayList<SyncJobData> getStatisticsData() {
        return statisticsData;
    }

    public void setStatisticsData(ArrayList<SyncJobData> statisticsData) {
        this.statisticsData = statisticsData;
    }

    public ArrayList<HashMap<String, String>> getWaste() {
        return waste;
    }

    public void setWaste(ArrayList<HashMap<String, String>> waste) {
        this.waste = waste;
    }

    public ArrayList<SyncJobData> getWasteData() {
        return wasteData;
    }

    public void setWasteData(ArrayList<SyncJobData> wasteData) {
        this.wasteData = wasteData;
    }
}

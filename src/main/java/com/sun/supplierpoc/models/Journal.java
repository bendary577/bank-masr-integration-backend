package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.MajorGroup;

import java.util.ArrayList;

public class Journal {

    private MajorGroup majorGroup;
    private String overGroup;
    private float totalWaste;
    private float totalCost;
    private float totalVariance;
    private float totalTransfer;
    private CostCenter costCenter;

    public Journal() {
    }

    private Journal(String overGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer) {
        this.overGroup = overGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
    }

    private Journal(MajorGroup majorGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer,
                    CostCenter costCenter) {
        this.majorGroup = majorGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
        this.costCenter = costCenter;
    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, MajorGroup majorGroup, float waste, float cost,
                                             float variance, float transfer, CostCenter costCenter){

        for (Journal journal:journals) {
            if(journal.majorGroup.getMajorGroup().equals(majorGroup.getMajorGroup())){
                return journals;
            }
        }

        journals.add(new Journal(majorGroup, waste, cost, variance, transfer, costCenter));
        return journals;

    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, String overGroup, float waste, float cost,
                                             float variance, float transfer){

        for (Journal journal:journals) {
            if(journal.overGroup.equals(overGroup)){
                journal.totalCost += cost;
                journal.totalTransfer += transfer;
                journal.totalVariance += variance;
                journal.totalWaste += waste;

                return journals;
            }
        }
        journals.add(new Journal(overGroup, waste, cost, variance, transfer));
        return journals;

    }

    public MajorGroup getMajorGroup() {
        return majorGroup;
    }

    public void setMajorGroup(MajorGroup majorGroup) {
        this.majorGroup = majorGroup;
    }

    public String getOverGroup() {
        return overGroup;
    }

    public void setOverGroup(String overGroup) {
        this.overGroup = overGroup;
    }

    public float getTotalWaste() {
        return totalWaste;
    }

    public void setTotalWaste(float totalWaste) {
        this.totalWaste = totalWaste;
    }

    public float getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(float totalCost) {
        this.totalCost = totalCost;
    }

    public float getTotalVariance() {
        return totalVariance;
    }

    public void setTotalVariance(float totalVariance) {
        this.totalVariance = totalVariance;
    }

    public float getTotalTransfer() {
        return totalTransfer;
    }

    public void setTotalTransfer(float totalTransfer) {
        this.totalTransfer = totalTransfer;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }
}

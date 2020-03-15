package com.sun.supplierpoc.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Journal {

    private String overGroup;
    private float totalWaste;
    private float totalCost;
    private float totalVariance;
    private float totalTransfer;

    public Journal() {
    }

    private Journal(String overGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer) {
        this.overGroup = overGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
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
}

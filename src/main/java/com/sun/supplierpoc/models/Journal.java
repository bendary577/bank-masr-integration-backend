package com.sun.supplierpoc.models;

import java.util.ArrayList;
import java.util.HashMap;

public class Journal {

    public String overGroup;
    public float totalWaste;
    public float totalCost;
    public float totalVariance;
    public float totalTransfer;

    public Journal() {
    }

    public Journal(String overGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer) {
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
}

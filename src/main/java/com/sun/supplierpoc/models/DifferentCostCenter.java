package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.CostCenter;

import java.util.ArrayList;

public class DifferentCostCenter {

    private CostCenter costCenter;
    private float totalTender;
    private float totalTax;
    private float totalOverGroupNet;

    public DifferentCostCenter() {
    }

    public DifferentCostCenter(CostCenter costCenter, float totalTender, float totalTax, float totalOverGroupNet) {
        this.costCenter = costCenter;
        this.totalTender = totalTender;
        this.totalTax = totalTax;
        this.totalOverGroupNet = totalOverGroupNet;
    }

    public ArrayList<DifferentCostCenter> checkExistence(ArrayList<DifferentCostCenter> differentCostCenters, CostCenter costCenter, float tender, float tax,
                                             float net){

        for (DifferentCostCenter differentCostCenter:differentCostCenters) {
            if(differentCostCenter.getCostCenter().costCenter.equals(costCenter.costCenter)){
                differentCostCenter.totalTender += tender;
                differentCostCenter.totalTax += tax;
                differentCostCenter.totalOverGroupNet += net;

                return differentCostCenters;
            }
        }
        differentCostCenters.add(new DifferentCostCenter(costCenter, tender, tax, net));
        return differentCostCenters;

    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public float getTotalTender() {
        return totalTender;
    }

    public void setTotalTender(float totalTender) {
        this.totalTender = totalTender;
    }

    public float getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(float totalTax) {
        this.totalTax = totalTax;
    }

    public float getTotalOverGroupNet() {
        return totalOverGroupNet;
    }

    public void setTotalOverGroupNet(float totalOverGroupNet) {
        this.totalOverGroupNet = totalOverGroupNet;
    }
}

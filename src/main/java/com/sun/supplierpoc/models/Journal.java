package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.services.InvoiceService;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Journal {

    private MajorGroup majorGroup;
    private FamilyGroup familyGroup;
    private String overGroup;

    private float totalWaste;
    private float totalCost;
    private float totalVariance;
    private float totalTransfer;
    private float tax;
    private float vat;
    private float net;

    private CostCenter costCenter;
    private RevenueCenter revenueCenter;
    private String departmentCode;

    public Journal() {
    }

    private Journal(String overGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer,
                    float tax) {
        this.overGroup = overGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
        this.tax = tax;
    }

    private Journal(String overGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer) {
        this.overGroup = overGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
    }

    private Journal(MajorGroup majorGroup, FamilyGroup familyGroup, float totalCost, CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {
        this.majorGroup = majorGroup;
        this.familyGroup = familyGroup;
        this.totalCost = totalCost;
        this.costCenter = costCenter;
        this.revenueCenter = revenueCenter;
        this.departmentCode = departmentCode;
    }

    private Journal(MajorGroup majorGroup, float totalWaste, float totalCost, float totalVariance, float totalTransfer,
                    CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {
        this.majorGroup = majorGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalVariance = totalVariance;
        this.totalTransfer = totalTransfer;
        this.costCenter = costCenter;
        this.revenueCenter = revenueCenter;
        this.departmentCode = departmentCode;
    }

    public Journal(String group) {
        this.overGroup = group;
    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, MajorGroup majorGroup,
                                             float waste, float cost, float variance, float transfer,
                                             CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {

        for (Journal journal : journals) {
            if (journal.majorGroup.getMajorGroup().equals(majorGroup.getMajorGroup())) {
                // Add new value
                journal.totalWaste += waste;
                journal.totalCost += cost;
                journal.totalVariance += variance;
                journal.totalTransfer += transfer;
                return journals;
            }
        }

        journals.add(new Journal(majorGroup, waste, cost, variance, transfer, costCenter, revenueCenter, departmentCode));
        return journals;

    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, MajorGroup majorGroup,
                                             String group ,float waste, float cost, float variance, float transfer,
                                             CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {

        for (Journal journal : journals) {
            if (journal.majorGroup.getMajorGroup().equals(group)) {
                // Add new value
                journal.totalWaste += waste;
                journal.totalCost += cost;
                journal.totalVariance += variance;
                journal.totalTransfer += transfer;
                return journals;
            }
        }

        journals.add(new Journal(majorGroup, waste, cost, variance, transfer, costCenter, revenueCenter, departmentCode));
        return journals;

    }
    public ArrayList<Journal> checkFGExistence(ArrayList<Journal> journals, MajorGroup majorGroup, FamilyGroup familyGroup,
                                               float cost, CostCenter costCenter, RevenueCenter revenueCenter,
                                               String departmentCode) {

        for (Journal journal : journals) {
            if (journal.familyGroup.familyGroup.equals(familyGroup.familyGroup)) {
                journal.totalCost += cost;
                return journals;
            }
        }

        journals.add(new Journal(majorGroup, familyGroup, cost, costCenter, revenueCenter, departmentCode));
        return journals;

    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, String overGroup, float waste, float cost,
                                             float variance, float transfer) {

        for (Journal journal : journals) {
            if (journal.overGroup.equals(overGroup)) {
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

    public RevenueCenter getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(RevenueCenter revenueCenter) {
        this.revenueCenter = revenueCenter;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public FamilyGroup getFamilyGroup() {
        return familyGroup;
    }

    public void setFamilyGroup(FamilyGroup familyGroup) {
        this.familyGroup = familyGroup;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public float getVat() {
        return vat;
    }

    public void setVat(float vat) {
        this.vat = vat;
    }

    public float getNet() {
        return net;
    }

    public void setNet(float net) {
        this.net = net;
    }
}

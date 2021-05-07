package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;

import java.util.ArrayList;

public class Journal {

    private MajorGroup majorGroup;
    private FamilyGroup familyGroup;
    private OrderType orderType;
    private String overGroup;

    private float totalWaste;
    private float totalCost;
    private float totalTransfer;
    private float tax;
    private float vat;
    private float net;

    private CostCenter costCenter;
    private RevenueCenter revenueCenter;
    private String departmentCode;
    private String DCMarker = "";

    public Journal() { }

    // Consumption Controller
    public Journal(String overGroup, float totalCost, CostCenter costCenter, String DCMarker) {
        this.overGroup = overGroup;
        this.totalCost = totalCost;
        this.costCenter = costCenter;
        this.DCMarker = DCMarker;
    }

    private Journal(String overGroup, float totalWaste, float totalCost, float totalTransfer) {
        this.overGroup = overGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalTransfer = totalTransfer;
    }

    private Journal(MajorGroup majorGroup, FamilyGroup familyGroup,
                    float totalCost, CostCenter costCenter, RevenueCenter revenueCenter, OrderType orderType, String departmentCode) {
        this.majorGroup = majorGroup;
        this.familyGroup = familyGroup;
        this.totalCost = totalCost;
        this.costCenter = costCenter;
        this.revenueCenter = revenueCenter;
        this.departmentCode = departmentCode;
        this.orderType = orderType;
    }

    private Journal(MajorGroup majorGroup, float totalCost,
                    CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode, OrderType orderType, String DCMarker) {
        this.majorGroup = majorGroup;
        this.totalCost = totalCost;
        this.costCenter = costCenter;
        this.revenueCenter = revenueCenter;
        this.departmentCode = departmentCode;
        this.DCMarker = DCMarker;
        this.orderType = orderType;
    }

    private Journal(MajorGroup majorGroup, float totalWaste, float totalCost, float totalTransfer,
                    CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {
        this.majorGroup = majorGroup;
        this.totalWaste = totalWaste;
        this.totalCost = totalCost;
        this.totalTransfer = totalTransfer;
        this.costCenter = costCenter;
        this.revenueCenter = revenueCenter;
        this.departmentCode = departmentCode;
    }

    public Journal(String group) {
        this.overGroup = group;
    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, MajorGroup majorGroup,
                                             float waste, float cost, float transfer,
                                             CostCenter costCenter, RevenueCenter revenueCenter, String departmentCode) {

        for (Journal journal : journals) {
            if (journal.majorGroup.getMajorGroup().equals(majorGroup.getMajorGroup())) {
                // Add new value
                journal.totalWaste += waste;
                journal.totalCost += cost;
                journal.totalTransfer += transfer;
                return journals;
            }
        }

        journals.add(new Journal(majorGroup, waste, cost, transfer, costCenter, revenueCenter, departmentCode));
        return journals;

    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, MajorGroup majorGroup, float cost,
                                             CostCenter costCenter, RevenueCenter revenueCenter,
                                             OrderType orderType ,String departmentCode,
                                             String DCMarker) {

        for (Journal journal : journals) {
            if (journal.getMajorGroup().getMajorGroup().equals(majorGroup.getMajorGroup()) && journal.departmentCode.equals(departmentCode)) {
                if(journal.getRevenueCenter() != null && journal.getRevenueCenter().getRevenueCenter().equals(revenueCenter.getRevenueCenter())){
                    if(orderType == null || (journal.orderType.getOrderType().equals(orderType.getOrderType()))){
                        journal.totalCost += cost;
                        return journals;
                    }
                }
            }
        }

        journals.add(new Journal(majorGroup, cost, costCenter, revenueCenter, departmentCode, orderType, DCMarker));
        return journals;
    }

    public ArrayList<Journal> checkFGExistence(ArrayList<Journal> journals, MajorGroup majorGroup, FamilyGroup familyGroup,
                                               float cost, CostCenter costCenter, RevenueCenter revenueCenter,
                                               OrderType orderType,
                                               String departmentCode) {

        for (Journal journal : journals) {
            if(journal.familyGroup != null){
                if (journal.familyGroup.familyGroup.equals(familyGroup.familyGroup)) {
                    if(journal.revenueCenter.getRevenueCenter().equals(revenueCenter.getRevenueCenter())){
                        if(orderType == null || (journal.orderType.getOrderType().equals(orderType.getOrderType()))){
                            journal.totalCost += cost;
                            return journals;
                        }
                    }
                }
            }
        }

        journals.add(new Journal(majorGroup, familyGroup, cost, costCenter, revenueCenter, orderType, departmentCode));
        return journals;

    }

    public ArrayList<Journal> checkExistence(ArrayList<Journal> journals, String overGroup, float waste, float cost,
                                             float transfer) {

        for (Journal journal : journals) {
            if (journal.overGroup.equals(overGroup)) {
                journal.totalCost += cost;
                journal.totalTransfer += transfer;
                journal.totalWaste += waste;

                return journals;
            }
        }
        journals.add(new Journal(overGroup, waste, cost, transfer));
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

    public String getDCMarker() {
        return DCMarker;
    }

    public void setDCMarker(String DCMarker) {
        this.DCMarker = DCMarker;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}

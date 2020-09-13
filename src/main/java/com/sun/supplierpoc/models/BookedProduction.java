package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.CostCenter;

public class BookedProduction {
    private String name;
    private String Status;
    private float Value;
    private String Date;
    private CostCenter costCenter;

    public BookedProduction() {
    }

    public BookedProduction(String name, String status, float value, String date, CostCenter costCenter) {
        this.name = name;
        Status = status;
        Value = value;
        Date = date;
        this.costCenter = costCenter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public float getValue() {
        return Value;
    }

    public void setValue(float value) {
        Value = value;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }
}

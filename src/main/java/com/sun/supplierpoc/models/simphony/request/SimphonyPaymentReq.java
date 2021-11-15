package com.sun.supplierpoc.models.simphony.request;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class SimphonyPaymentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String employeeId;
    private String revenueCentreName;
    private int revenueCentreId;
    private String checkNumber;

    public SimphonyPaymentReq() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getRevenueCentreName() {
        return revenueCentreName;
    }

    public void setRevenueCentreName(String revenueCentreName) {
        this.revenueCentreName = revenueCentreName;
    }

    public int getRevenueCentreId() {
        return revenueCentreId;
    }

    public void setRevenueCentreId(int revenueCentreId) {
        this.revenueCentreId = revenueCentreId;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
}
package com.sun.supplierpoc.models.simphony.SplittableCheck;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class SplittableCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String checkValue;
    private String checkNumber;
    private String employeeId;
    private String revenueCentreName;
    private String revenueCentreId ;
    private int splitNumber;

    public SplittableCheck() {
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
    public String getCheckValue() {
        return checkValue;
    }
    public void setCheckValue(String checkValue) {
        this.checkValue = checkValue;
    }
    public String getCheckNumber() {
        return checkNumber;
    }
    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
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
    public String getRevenueCentreId() {
        return revenueCentreId;
    }
    public void setRevenueCentreId(String revenueCentreId) {
        this.revenueCentreId = revenueCentreId;
    }
    public int getSplitNumber() {
        return splitNumber;
    }
    public void setSplitNumber(int splitNumber) {
        this.splitNumber = splitNumber;
    }
}

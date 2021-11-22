package com.sun.supplierpoc.models.simphony.SplittableCheck;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class SimphonyPaymentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

//    @NotNull(message = "Employee ID can't be empty.")
    private String employeeId;

//    @NotNull(message = "Revenue Center Name can't be empty.")
    private String revenueCentreName;

    @NotNull(message = "Revenue Center ID can't be empty.")
    private int revenueCentreId;

//    @NotNull(message = "Employee ID can't be empty.")
    private String checkNumber;

//    @NotNull(message = "Employee ID can't be empty.")
    private String totalDue;

//    @NotNull(message = "Employee ID can't be empty.")
    private int cashierNumber;

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

    public String getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(String totalDue) {
        this.totalDue = totalDue;
    }

    public int getCashierNumber() {
        return cashierNumber;
    }

    public void setCashierNumber(int cashierNumber) {
        this.cashierNumber = cashierNumber;
    }
}
package com.sun.supplierpoc.models.configurations;

public class SimphonyLocation {
    private boolean checked;
    private int employeeNumber;
    private int revenueCenterID;
    private String revenueCenterName;
    private String simphonyServer = "";

    public SimphonyLocation() {
    }

    public SimphonyLocation(boolean checked, int employeeNumber, int revenueCenterID, String revenueCenterName) {
        this.checked = checked;
        this.employeeNumber = employeeNumber;
        this.revenueCenterID = revenueCenterID;
        this.revenueCenterName = revenueCenterName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(int employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public int getRevenueCenterID() {
        return revenueCenterID;
    }

    public void setRevenueCenterID(int revenueCenterID) {
        this.revenueCenterID = revenueCenterID;
    }

    public String getRevenueCenterName() {
        return revenueCenterName;
    }

    public void setRevenueCenterName(String revenueCenterName) {
        this.revenueCenterName = revenueCenterName;
    }

    public String getSimphonyServer() {
        return simphonyServer;
    }

    public void setSimphonyServer(String simphonyServer) {
        this.simphonyServer = simphonyServer;
    }


}

package com.sun.supplierpoc.models.configurations;

import java.util.List;

public class SalesAPIStatistics {

    public boolean checked = false;
    public String location = "";

    public String leaseCode= "";
    public String unitNo = "";
    public String brand = "";
    public String registeredName = "";
    public String NoChecks = "";
    public String NetSales = "";
    public String dateFrom = "";
    public String dateTo = "";
    public List<OrderTypeChannels> orderTypeChannels;


    public SalesAPIStatistics() {
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLeaseCode() {
        return leaseCode;
    }

    public void setLeaseCode(String leaseCode) {
        this.leaseCode = leaseCode;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getRegisteredName() {
        return registeredName;
    }

    public void setRegisteredName(String registeredName) {
        this.registeredName = registeredName;
    }

    public String getNoChecks() {
        return NoChecks;
    }

    public void setNoChecks(String noChecks) {
        NoChecks = noChecks;
    }

    public String getNetSales() {
        return NetSales;
    }

    public void setNetSales(String netSales) {
        NetSales = netSales;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public List<OrderTypeChannels> getOrderTypeChannels() {
        return orderTypeChannels;
    }

    public void setOrderTypeChannels(List<OrderTypeChannels> orderTypeChannels) {
        this.orderTypeChannels = orderTypeChannels;
    }
}

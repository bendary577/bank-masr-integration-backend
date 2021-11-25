package com.sun.supplierpoc.models.simphony.simphonyCheck;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document
public class SimphonyCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String checkValue;

    private String checkNumber;

    private String employeeId;

    private String employeeName;

    private String revenueCenterName;

    private int revenueCenterId;

    private int cashierNumber;

    private String tips;

    private String accountId;

    private boolean payed;

    private Date creationDate;

    private Date lastUpdate;

    private List<TransactionResponse> transactionResponses = new ArrayList<>();

    public SimphonyCheck() {
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

    public String getRevenueCenterName() {
        return revenueCenterName;
    }

    public void setRevenueCenterName(String revenueCenterName) {
        this.revenueCenterName = revenueCenterName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public int getRevenueCenterId() {
        return revenueCenterId;
    }

    public void setRevenueCenterId(int revenueCenterId) {
        this.revenueCenterId = revenueCenterId;
    }

    public int getCashierNumber() {
        return cashierNumber;
    }

    public void setCashierNumber(int cashierNumber) {
        this.cashierNumber = cashierNumber;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public List<TransactionResponse> getTransactionResponses() {
        return transactionResponses;
    }

    public void setTransactionResponses(List<TransactionResponse> transactionResponses) {
        this.transactionResponses = transactionResponses;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isPayed() {
        return payed;
    }

    public void setPayed(boolean payed) {
        this.payed = payed;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

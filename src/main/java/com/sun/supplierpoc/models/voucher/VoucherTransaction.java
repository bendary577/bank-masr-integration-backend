package com.sun.supplierpoc.models.voucher;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document
public class VoucherTransaction {

    @Id
    private String id;
    private String status;
    private String reason;
    @NotNull(message="Code can't be empty.")
    @NotBlank(message="Code can't be empty.")
    private String code;
    private Date transactionDate;
    @NotNull(message="Revenue Centre Id can't be empty.")
    private int revenueCentreId;
    private String accountId;
    private String voucherId;
    private String transactionTypeId;
    private String revenueCentreName;
    private String employeeId;
    private String checkNumber;
    private double totalAmount;
    private double afterDiscount;
    private double discountRate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public int getRevenueCentreId() {
        return revenueCentreId;
    }

    public void setRevenueCentreId(int revenueCentreId) {
        this.revenueCentreId = revenueCentreId;
    }

    public String getRevenueCentreName() {
        return revenueCentreName;
    }

    public void setRevenueCentreName(String revenueCentreName) {
        this.revenueCentreName = revenueCentreName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getAfterDiscount() {
        return afterDiscount;
    }

    public void setAfterDiscount(double afterDiscount) {
        this.afterDiscount = afterDiscount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(String transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}



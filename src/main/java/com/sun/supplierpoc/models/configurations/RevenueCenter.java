package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;
import java.util.List;

public class RevenueCenter {
    private boolean checked = false;
    private int revenueCenterId;
    private String revenueCenter = "";
    private String revenueCenterReference = "";
    private String discountAccount ="";
    private String accountCode ="";
    private boolean requireAnalysis = false;

    private List<OrderType> orderTypes = new ArrayList<>();

    public RevenueCenter() {
    }

    public RevenueCenter(boolean requireAnalysis) {
        this.requireAnalysis = requireAnalysis;
    }

    public RevenueCenter(boolean checked, String revenueCenter) {
        this.checked = checked;
        this.revenueCenter = revenueCenter;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(String revenueCenter) {
        this.revenueCenter = revenueCenter;
    }

    public String getRevenueCenterReference() {
        return revenueCenterReference;
    }

    public void setRevenueCenterReference(String revenueCenterReference) {
        this.revenueCenterReference = revenueCenterReference;
    }

    public String getDiscountAccount() {
        return discountAccount;
    }

    public void setDiscountAccount(String discountAccount) {
        this.discountAccount = discountAccount;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public List<OrderType> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(List<OrderType> orderTypes) {
        this.orderTypes = orderTypes;
    }

    public boolean isRequireAnalysis() {
        return requireAnalysis;
    }

    public void setRequireAnalysis(boolean requireAnalysis) {
        this.requireAnalysis = requireAnalysis;
    }

    public int getRevenueCenterId() {
        return revenueCenterId;
    }

    public void setRevenueCenterId(int revenueCenterId) {
        this.revenueCenterId = revenueCenterId;
    }
}

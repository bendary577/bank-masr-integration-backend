package com.sun.supplierpoc.models.configurations;

public class RevenueCenter {
    private boolean checked = false;
    private String revenueCenter = "";
    private String discountAccount ="";
    private String accountCode ="";

    public RevenueCenter() {
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
}

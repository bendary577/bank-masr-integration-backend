package com.sun.supplierpoc.models.configurations;

public class RevenueCenter {
    private boolean checked = false;
    private String revenueCenter = "";

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
}

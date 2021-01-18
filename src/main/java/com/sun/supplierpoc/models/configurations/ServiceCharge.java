package com.sun.supplierpoc.models.configurations;

public class ServiceCharge {
    private boolean checked = false;
    private String serviceCharge = "";
    private String account = "";
    private Float total;

    private CostCenter costCenter = new CostCenter();
    private RevenueCenter revenueCenter = new RevenueCenter();

    public ServiceCharge() {
    }

    public String getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public RevenueCenter getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(RevenueCenter revenueCenter) {
        this.revenueCenter = revenueCenter;
    }
}

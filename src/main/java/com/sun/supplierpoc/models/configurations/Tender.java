package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class Tender implements Serializable {
    @Id
    private String id;
    private boolean checked = false;
    private String tender = "";
    private String account = "";
    private Float total;
    private CostCenter costCenter;

    public Tender() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTender() {
        return tender;
    }

    public void setTender(String tender) {
        this.tender = tender;
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
}

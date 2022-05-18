package com.sun.supplierpoc.models.configurations;
import java.io.Serializable;

public class CostCenterAccountCodeMapping {

    private String costCenter = "";
    private String accountCode = "";

    public CostCenterAccountCodeMapping() {
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }
}

package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tender implements Serializable {
    private boolean checked = false;
    private String tender = "";
    private String account = "";
    private float total = 0;

    private String communicationTender = "";
    private String communicationAccount = "";
    private float communicationRate;

    private String analysisCodeT5 = "";

    private CostCenter costCenter;
    private RevenueCenter revenueCenter;

    private List<String> children = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Tender() {
        communicationRate = 0;
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

    public String getCommunicationTender() {
        return communicationTender;
    }

    public void setCommunicationTender(String communicationTender) {
        this.communicationTender = communicationTender;
    }

    public String getCommunicationAccount() {
        return communicationAccount;
    }

    public void setCommunicationAccount(String communicationAccount) {
        this.communicationAccount = communicationAccount;
    }

    public float getCommunicationRate() {
        return communicationRate;
    }

    public void setCommunicationRate(float communicationRate) {
        this.communicationRate = communicationRate;
    }

    public String getAnalysisCodeT5() {
        return analysisCodeT5;
    }

    public void setAnalysisCodeT5(String analysisCodeT5) {
        this.analysisCodeT5 = analysisCodeT5;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public RevenueCenter getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(RevenueCenter revenueCenter) {
        this.revenueCenter = revenueCenter;
    }
}

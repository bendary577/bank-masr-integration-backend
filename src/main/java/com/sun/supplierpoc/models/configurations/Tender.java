package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;

public class Tender implements Serializable {
    @Id
    private String id;
    private boolean checked = false;
    private String tender = "";
    private String account = "";

    private String communicationTender = "";
    private String communicationAccount = "";
    private float communicationRate;

    private String analysisCodeT5 = "";

    private Float total;
    private CostCenter costCenter;

    public Tender() {
        communicationRate = 0;
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

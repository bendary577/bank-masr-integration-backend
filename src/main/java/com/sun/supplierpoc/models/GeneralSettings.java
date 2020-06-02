package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.CostCenter;
import com.sun.supplierpoc.models.configurations.OverGroup;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class GeneralSettings {
    @Id
    private String id;
    private String accountId;
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenterLocationMapping = new ArrayList<>();
    private Date creationDate;
    private boolean deleted;

    public GeneralSettings() {
    }

    public GeneralSettings(String accountId, ArrayList<OverGroup> overGroups, ArrayList<CostCenter> costCenterLocationMapping, Date creationDate, boolean deleted) {
        this.accountId = accountId;
        this.overGroups = overGroups;
        this.costCenterLocationMapping = costCenterLocationMapping;
        this.creationDate = creationDate;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public ArrayList<OverGroup> getOverGroups() {
        return overGroups;
    }

    public void setOverGroups(ArrayList<OverGroup> overGroups) {
        this.overGroups = overGroups;
    }

    public ArrayList<CostCenter> getCostCenterLocationMapping() {
        return costCenterLocationMapping;
    }

    public void setCostCenterLocationMapping(ArrayList<CostCenter> costCenterLocationMapping) {
        this.costCenterLocationMapping = costCenterLocationMapping;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

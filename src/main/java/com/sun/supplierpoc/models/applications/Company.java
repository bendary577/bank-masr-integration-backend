package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Company implements Serializable {
    @Id
    private String id;

    private String name;
    private String description;
    private float discountRate;
    private String logoUrl;
    private String accountID;


    private ArrayList<Group> groups = new ArrayList<>();

    private Date creationDate;
    private Date lastUpdate;
    private boolean deleted;

    public Company(String name, String description, float discountRate, String logoUrl, String accountID, Date creationDate, Date lastUpdate, boolean deleted) {
        this.name = name;
        this.description = description;
        this.discountRate = discountRate;
        this.logoUrl = logoUrl;
        this.accountID = accountID;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(float discountRate) {
        this.discountRate = discountRate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }
}

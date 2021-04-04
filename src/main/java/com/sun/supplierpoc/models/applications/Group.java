package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {
    @Id
    private String id;
    private Group parentGroup;
    private String name;
    private String description;
    private float discountRate;
    private int discountId;
    private int top;
    private String logoUrl;
    private String accountID;
    private Date creationDate;
    private Date lastUpdate;
    private boolean deleted;

    public Group() {
    }

    public Group(String name, String description, float discountRate, String logoUrl, String accountID, Date creationDate, Date lastUpdate, boolean deleted) {
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

    public Group getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}


package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class SyncJobType implements Serializable {
    @Id
    private String id;
    private String name;
    private String description;
    private String endPoint;
    private Date creationDate;
    private Configuration configuration;
    private String accountId;
    private boolean deleted;

    public SyncJobType() {
    }

    public SyncJobType(String name, String description, String endPoint, Date creationDate, Configuration configuration,
                       String accountId) {
        this.name = name;
        this.description = description;
        this.endPoint = endPoint;
        this.creationDate = creationDate;
        this.configuration = configuration;
        this.accountId = accountId;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

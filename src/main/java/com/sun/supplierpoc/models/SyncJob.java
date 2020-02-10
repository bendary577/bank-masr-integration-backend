package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class SyncJob implements Serializable {

    @Id
    private String id;
    private String status;
    private Date creationDate;
    private Date endDate;
    private String userId;
    private String accountId;
    private String syncJobTypeId;
    private boolean deleted;

    public SyncJob() {
    }

    public SyncJob(String status, Date creationDate, Date endDate, String userId, String accountId,
                   String syncJobTypeId) {
        this.status = status;
        this.creationDate = creationDate;
        this.endDate = endDate;
        this.userId = userId;
        this.accountId = accountId;
        this.syncJobTypeId = syncJobTypeId;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getSyncJobTypeId() {
        return syncJobTypeId;
    }

    public void setSyncJobTypeId(String syncJobTypeId) {
        this.syncJobTypeId = syncJobTypeId;
    }


}

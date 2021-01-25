package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


public class SyncJobData implements Serializable {

    @Id
    private String id;
    private HashMap<String, String> data;
    private HashMap<String, Object> data2;

    private String status;
    private String reason;
    private Date creationDate;
    private String syncJobId;
    private boolean deleted;

    public SyncJobData() {
    }

    public SyncJobData(HashMap<String, String> data, String status, String reason, Date creationDate, String syncJobId) {
        this.data = data;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.syncJobId = syncJobId;
        this.deleted = false;
    }

    public SyncJobData(String status, HashMap<String, Object> data, String reason, Date creationDate, String syncJobId) {
        this.data2 = data;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.syncJobId = syncJobId;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSyncJobId() {
        return syncJobId;
    }

    public void setSyncJobId(String syncJobId) {
        this.syncJobId = syncJobId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public HashMap<String, Object> getData2() {
        return data2;
    }

    public void setData2(HashMap<String, Object> data2) {
        this.data2 = data2;
    }
}

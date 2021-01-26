package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


@Document(collection = "operationData")
public class OperationData implements Serializable {

    @Id
    private String id;
    private HashMap<String, Object> data;
    private String status;
    private String reason;
    private Date creationDate;
    private String operationId;
    private boolean deleted;

    public OperationData() {
    }

    public OperationData(HashMap<String, Object> data, String status, String reason, Date creationDate, String operationId) {
        this.data = data;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.operationId = operationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
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

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

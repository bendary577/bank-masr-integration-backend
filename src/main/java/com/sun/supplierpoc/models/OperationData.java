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
    private Date creationDate;
    private String operationId;
    private boolean deleted;

    public OperationData() {
    }

    public OperationData(HashMap<String, Object> data, Date creationDate, String operationId) {
        this.data = data;
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

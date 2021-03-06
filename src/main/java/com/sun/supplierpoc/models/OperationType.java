package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.operationConfiguration.OperationConfiguration;
import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.util.Date;

public class OperationType implements Serializable {
    @Id
    private String id;
    private int index;
    private String name;
    private String endPoint;
    private Date creationDate;
    private OperationConfiguration configuration;
    private String accountId;
    private boolean deleted;

    public OperationType() {
    }

    public OperationType(int index, String name, String endPoint, Date creationDate, String accountId) {
        this.index = index;
        this.name = name;
        this.endPoint = endPoint;
        this.creationDate = creationDate;
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public OperationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(OperationConfiguration configuration) {
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

package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection = "operation")
public class Operation implements Serializable {

    @Id
    private String id;
    private String status;
    private String reason;
    private Date creationDate;
    private Date endDate;
    private String userId;
    private String accountId;
    private String operationTypeId;
    private int rowsFetched;
    private int revenueCenter;
    private boolean deleted;

    public Operation(String status, String reason, Date creationDate,
                     Date endDate, String userId, String accountId,
                     String operationTypeId, int revenueCenter, boolean deleted) {
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.endDate = endDate;
        this.userId = userId;
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.revenueCenter = revenueCenter;
        this.deleted = deleted;
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

    public String getOperationTypeId() {
        return operationTypeId;
    }

    public void setOperationTypeId(String operationTypeId) {
        this.operationTypeId = operationTypeId;
    }

    public int getRowsFetched() {
        return rowsFetched;
    }

    public void setRowsFetched(int rowsFetched) {
        this.rowsFetched = rowsFetched;
    }

    public int getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(int revenueCenter) {
        this.revenueCenter = revenueCenter;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

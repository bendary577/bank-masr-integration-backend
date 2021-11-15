package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.operationConfiguration.OperationConfiguration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

//@Document(collation = "transactionType")
public class TransactionType {
    @Id
    private String id;
    private int index;
    @NotNull(message = "Name can't be empty.")
    @NotBlank(message = "Name can't be empty.")
    private String name;
    private String endPoint;
    private Date creationDate;
    private OperationConfiguration configuration;
    private String accountId;
    private boolean deleted;

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

package com.sun.supplierpoc.models.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "InvokerUser")
public class InvokerUser implements Serializable {

    @Id
    private String id;
    private String username ;
    private String password;
    private String accountId;
    private List<String> typeId = new ArrayList<>();
    private boolean deleted;
    private Date creationDate;

    public InvokerUser() {
    }

    public InvokerUser(String id) {
        this.id = id;
    }

    public InvokerUser(String username, String password, String accountId, List<String> typeId,
                       Date creationDate) {
        this.username = username;
        this.password = password;
        this.accountId = accountId;
        this.creationDate = creationDate;
        this.typeId = typeId;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<String> getTypeId() {
        return typeId;
    }

    public void setTypeId(List<String> typeId) {
        this.typeId = typeId;
    }
}

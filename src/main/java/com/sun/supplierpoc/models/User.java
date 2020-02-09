package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    @Id
    public String id;
    public String name;
    public String accountId;
    public String username;
    public String password;
    public Date creationDate;
    public boolean deleted;

    public User() {
    }

    public User(String id, String name, String accountId, String username, String password, Date creationDate,
                boolean deleted) {
        this.id = id;
        this.name = name;
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.creationDate = creationDate;
        this.deleted = deleted;
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

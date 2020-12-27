package com.sun.supplierpoc.models.auth;

import com.sun.supplierpoc.models.roles.UserAccess;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;

@Document(collection = "InvokerUser")
public class InvokerUser {
    @Id
    private String id;
    private String username ;
    private String password;
    private String accountId;
    private boolean deleted;
    private Date creationDate;
    private ArrayList<UserAccess> userAccesses;

    public InvokerUser() {
    }

    public InvokerUser(String id, String username, String password, String accountId, boolean deleted,
                       Date creationDate, ArrayList<UserAccess> userAccesses) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.accountId = accountId;
        this.deleted = deleted;
        this.creationDate = creationDate;
        this.userAccesses = userAccesses;
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

    public ArrayList<UserAccess> getUserAccesses() {
        return userAccesses;
    }

    public void setUserAccesses(ArrayList<UserAccess> userAccesses) {
        this.userAccesses = userAccesses;
    }
}

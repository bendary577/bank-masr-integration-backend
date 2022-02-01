package com.sun.supplierpoc.models.applications;

import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

public class Action {
    @Id
    private String id;
    private String actionType;
    private double amount;

    @DBRef
    private User user;
    @DBRef
    private ApplicationUser applicationUser;

    private String accountId;

    private Date date;
    private boolean deleted;

    public Action() {
    }

    public Action(String actionType, double amount, User user, ApplicationUser applicationUser, String accountId, Date date) {
        this.actionType = actionType;
        this.amount = amount;
        this.user = user;
        this.applicationUser = applicationUser;
        this.accountId = accountId;
        this.date = date;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ApplicationUser getApplicationUser() {
        return applicationUser;
    }

    public void setApplicationUser(ApplicationUser applicationUser) {
        this.applicationUser = applicationUser;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

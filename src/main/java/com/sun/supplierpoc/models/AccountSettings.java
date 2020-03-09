package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class AccountSettings implements Serializable {
    @Id
    private String id = "";
    private String wastageGroupIdStarting = "";
    private String transferGroupIdStarting = "";


    public AccountSettings() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWastageGroupIdStarting() {
        return wastageGroupIdStarting;
    }

    public void setWastageGroupIdStarting(String wastageGroupIdStarting) {
        this.wastageGroupIdStarting = wastageGroupIdStarting;
    }

    public String getTransferGroupIdStarting() {
        return transferGroupIdStarting;
    }

    public void setTransferGroupIdStarting(String transferGroupIdStarting) {
        this.transferGroupIdStarting = transferGroupIdStarting;
    }
}

package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Account implements Serializable {
    @Id
    private String id;
    private String name;
    private String domain;
    private String ERD;
    private ArrayList<AccountCredential> accountCredentials;
    private int locationQuota;
    private Date creationDate;
    private boolean deleted;

    public Account() {
    }

    public Account(String id, String name, String domain, String ERD, ArrayList<AccountCredential> accountCredentials,
                   int locationQuota, Date creationDate, boolean deleted) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.ERD = ERD;
        this.accountCredentials = accountCredentials;
        this.locationQuota = locationQuota;
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getERD() {
        return ERD;
    }

    public void setERD(String ERD) {
        this.ERD = ERD;
    }

    public ArrayList<AccountCredential> getAccountCredentials() {
        return accountCredentials;
    }

    public void setAccountCredentials(ArrayList<AccountCredential> accountCredentials) {
        this.accountCredentials = accountCredentials;
    }

    public int getLocationQuota() {
        return locationQuota;
    }

    public void setLocationQuota(int locationQuota) {
        this.locationQuota = locationQuota;
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

    public AccountCredential getAccountCredentialByAccount(String accountName, ArrayList<AccountCredential> accountCredentials){
        for (AccountCredential accountCredential : accountCredentials) {
            if (accountCredential.getAccount().equals(accountName)) {
                return accountCredential;
            }
        }
        return new AccountCredential();
    }
}

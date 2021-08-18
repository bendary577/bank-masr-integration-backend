package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.AccountCredential;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account implements Serializable {
    @Id
    private String id;
    private String name;
    private String imageUrl;
    private String domain;
    private String ERD;
    /*
     * Version 1: https://mte03-ohra-prod.hospitality.oracleindustry.com/mainPortal.jsp
     * Version 2: https://mte4-ohra-idm.oracleindustry.com/oidc-ui/
     * */
    private String microsVersion;
    private ArrayList<AccountCredential> accountCredentials;
    private int locationQuota;
    private Date creationDate;
    private boolean deleted;
    private AccountEmailConfig emailConfig;

    @DBRef
    private List<Feature> features;

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

    public String getMicrosVersion() {
        return microsVersion;
    }

    public void setMicrosVersion(String microsVersion) {
        this.microsVersion = microsVersion;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public AccountEmailConfig getEmailConfig() {
        return emailConfig;
    }

    public void setEmailConfig(AccountEmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}


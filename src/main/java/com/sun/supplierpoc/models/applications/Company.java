package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class Company implements Serializable {
    @Id
    private String id;
    private String name;
    private String logoUrl;
    private Date creationDate;
    private boolean deleted;

    public Company(String name, String logoUrl, Date creationDate, boolean deleted) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.creationDate = creationDate;
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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

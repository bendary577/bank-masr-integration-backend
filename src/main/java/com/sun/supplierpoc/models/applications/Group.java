package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.Date;

public class Group implements Serializable {
    @Id
    private String id;
    private String name;

    @DBRef
    private Company companyId;

    private Date creationDate;
    private boolean deleted;

    public Group(String name, Company companyId, Date creationDate, boolean deleted) {
        this.name = name;
        this.companyId = companyId;
        this.creationDate = creationDate;
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Company getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Company companyId) {
        this.companyId = companyId;
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

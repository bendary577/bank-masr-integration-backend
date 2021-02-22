package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.Serializable;
import java.util.Date;

public class ApplicationUser implements Serializable {
    @Id
    private String id;
    private String name;

    @DBRef
    private Company company;

    @DBRef
    private Group group;

    private Date creationDate;
    private boolean deleted;

    public ApplicationUser(String name, Company company, Group group, Date creationDate, boolean deleted) {
        this.name = name;
        this.company = company;
        this.group = group;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
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

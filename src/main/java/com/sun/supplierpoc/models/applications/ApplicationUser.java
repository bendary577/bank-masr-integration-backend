package com.sun.supplierpoc.models.applications;

import org.springframework.data.annotation.Id;
import java.io.Serializable;
import java.util.Date;

public class ApplicationUser implements Serializable {
    @Id
    private String id;
    private String name;

    private Group group;
    private Company company;

    private Date creationDate;
    private Date lastUpdate;
    private boolean deleted;

    public ApplicationUser(String name, Group group, Company company, Date creationDate, Date lastUpdate, boolean deleted) {
        this.name = name;
        this.group = group;
        this.company = company;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

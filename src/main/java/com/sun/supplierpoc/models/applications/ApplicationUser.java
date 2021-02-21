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
    private Company companyId;

    @DBRef
    private Group groupId;

    private Date creationDate;
    private boolean deleted;

    public ApplicationUser(String name, Company companyId, Group groupId, Date creationDate, boolean deleted) {
        this.name = name;
        this.companyId = companyId;
        this.groupId = groupId;
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

    public Group getGroupId() {
        return groupId;
    }

    public void setGroupId(Group groupId) {
        this.groupId = groupId;
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

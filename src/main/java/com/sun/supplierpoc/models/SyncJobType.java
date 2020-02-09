package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

public class SyncJobType implements Serializable {
    @Id
    private String id;
    private String name;
    private String description;
    private String endPoint;
    private Date creationDate;
    private Object configuration;
    private boolean deleted;

    public SyncJobType() {
    }

    public SyncJobType(String id, String name, String description, String endPoint, Date creationDate,
                       Object configuration, boolean deleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.endPoint = endPoint;
        this.creationDate = creationDate;
        this.configuration = configuration;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}

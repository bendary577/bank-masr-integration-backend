package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.opera.Reservation;
import com.sun.supplierpoc.models.simphony.response.MenuItemResponse;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class SyncJobData implements Serializable {

    @Id
    private String id;
    private HashMap<String, Object> data;
    private MenuItemResponse menuItemResponse;
    private Reservation reservation;
    private String status;
    private String reason;
    private Date creationDate;
    private String syncJobId;
    private boolean deleted;

    public SyncJobData() {
    }

    public SyncJobData(HashMap<String, Object> data, String status, String reason, Date creationDate, String syncJobId) {
        this.data = data;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.syncJobId = syncJobId;
        this.deleted = false;
    }

    public SyncJobData(Reservation reservation, String status, String reason, Date creationDate, String syncJobId, boolean deleted) {
        this.reservation = reservation;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.syncJobId = syncJobId;
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSyncJobId() {
        return syncJobId;
    }

    public void setSyncJobId(String syncJobId) {
        this.syncJobId = syncJobId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public MenuItemResponse getMenuItemResponse() {
        return menuItemResponse;
    }

    public void setMenuItemResponse(MenuItemResponse menuItemResponse) {
        this.menuItemResponse = menuItemResponse;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }
}

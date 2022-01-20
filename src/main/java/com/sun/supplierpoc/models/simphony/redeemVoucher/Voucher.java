package com.sun.supplierpoc.models.simphony.redeemVoucher;

import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document
public class Voucher {

    @Id
    private String id;

    private String name;

    private Date startDate;

    private Date endDate;

    private SimphonyDiscount simphonyDiscount;

    private List<UniqueVoucher> uniqueVouchers;

    private int redemption;

    private Date creationDate;

    private Date lastUpdate;

    private String accountId;

    private boolean deleted;

    public Voucher() {
    }

    public Voucher(String name, Date startDate, Date endDate, SimphonyDiscount simphonyDiscount,
                   List<UniqueVoucher> uniqueVouchers, Date creationDate, String accountId, boolean deleted) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.simphonyDiscount = simphonyDiscount;
        this.uniqueVouchers = uniqueVouchers;
        this.creationDate = creationDate;
        this.accountId = accountId;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public SimphonyDiscount getSimphonyDiscount() {
        return simphonyDiscount;
    }

    public void setSimphonyDiscount(SimphonyDiscount simphonyDiscount) {
        this.simphonyDiscount = simphonyDiscount;
    }

    public List<UniqueVoucher> getUniqueVouchers() {
        return uniqueVouchers;
    }

    public void setUniqueVouchers(List<UniqueVoucher> uniqueVouchers) {
        this.uniqueVouchers = uniqueVouchers;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getRedemption() {
        return redemption;
    }

    public void setRedemption(int redemption) {
        this.redemption = redemption;
    }
}

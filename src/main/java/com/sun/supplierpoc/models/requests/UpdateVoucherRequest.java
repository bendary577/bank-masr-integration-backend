package com.sun.supplierpoc.models.requests;

import com.sun.supplierpoc.models.applications.SimphonyDiscount;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class UpdateVoucherRequest {

    private String id;

    @NotBlank(message = "Voucher name can't be empty.")
    private String name;

    @NotNull(message = "Start Date must be configured.")
    private Date startDate;

    @NotNull(message = "End Date must be configured.")
    private Date endDate;

    @NotNull(message = "Simphony discount can't be empty.")
    private SimphonyDiscount simphonyDiscount;

    @NotBlank(message = "Voucher code can't be empty")
    private String voucherCode;

    @NotNull(message = "Redeem quota can't be empty")
    private int redeemQuota;

    @NotNull(message = "Creation date can't be empty.")
    private Date creationDate;

    @NotBlank(message = "Account ID can't be empty.")
    private String accountId;

    @NotNull(message = "Deleted status can't be empty.")
    private boolean deleted;

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

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public int getRedeemQuota() {
        return redeemQuota;
    }

    public void setRedeemQuota(int redeemQuota) {
        this.redeemQuota = redeemQuota;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
}

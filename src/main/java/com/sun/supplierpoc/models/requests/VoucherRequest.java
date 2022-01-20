package com.sun.supplierpoc.models.requests;

import com.sun.supplierpoc.models.simphony.redeemVoucher.UniqueVoucher;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class VoucherRequest {

    private String id;

    @NotBlank(message = "Voucher name can't be empty.")
    private String name;

    @NotNull(message = "Start Date must be configured.")
    private Date startDate;

    @NotNull(message = "End Date must be configured.")
    private Date endDate;

    @NotNull(message = "Simphony discount can't be empty.")
    private int simphonyDiscountId;

    @NotNull(message = "Unique Voucher can't be empty.")
    private int uniqueVouchers;

    @NotNull(message = "Redemption can't be empty.")
    private int redemption;

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

    public int getSimphonyDiscountId() {
        return simphonyDiscountId;
    }

    public void setSimphonyDiscountId(int simphonyDiscountId) {
        this.simphonyDiscountId = simphonyDiscountId;
    }

    public int getUniqueVouchers() {
        return uniqueVouchers;
    }

    public void setUniqueVouchers(int uniqueVouchers) {
        this.uniqueVouchers = uniqueVouchers;
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

package com.sun.supplierpoc.models.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

public class CreateVoucherRequest {

    private String id;

    @NotBlank(message = "Voucher name can't be empty.")
    private String name;

    @NotNull(message = "Start date must be configured.")
    private Date startDate;

    @NotNull(message = "End date must be configured.")
    private Date endDate;

    @NotNull(message = "Simphony discount can't be empty.")
    private int simphonyDiscountId;

    @NotNull(message = "Redeem Quota can't be empty.")
    private int redeemQuota;

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

    public int getRedeemQuota() {
        return redeemQuota;
    }

    public void setRedeemQuota(int redeemQuota) {
        this.redeemQuota = redeemQuota;
    }
}

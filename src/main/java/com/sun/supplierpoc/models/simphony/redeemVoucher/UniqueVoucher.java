package com.sun.supplierpoc.models.simphony.redeemVoucher;

public class UniqueVoucher {

    private String id;

    private String status;

    private int numOfRedemption;

    private String code;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumOfRedemption() {
        return numOfRedemption;
    }

    public void setNumOfRedemption(int numOfRedemption) {
        this.numOfRedemption = numOfRedemption;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}

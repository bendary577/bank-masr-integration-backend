package com.sun.supplierpoc.models.operationConfiguration;

public class OperationConfiguration {
    /*
    * Create open check Operation
    * */
    private String tenderNumber = "";
    private String accountType = "";
    private String discountNumber = "";

    public String getTenderNumber() {
        return tenderNumber;
    }

    public void setTenderNumber(String tenderNumber) {
        this.tenderNumber = tenderNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getDiscountNumber() {
        return discountNumber;
    }

    public void setDiscountNumber(String discountNumber) {
        this.discountNumber = discountNumber;
    }

}

package com.sun.supplierpoc.models.opera;

import java.io.Serializable;

public class TransactionObject implements Serializable {
    private String amount;
    private String ecr;
    private String payKind;
    private String cachierID;
    private String transCurrency;
    private String cachierID;


    public TransactionObject() {
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getEcr() {
        return ecr;
    }

    public void setEcr(String ecr) {
        this.ecr = ecr;
    }

    public String getPayKind() {
        return payKind;
    }

    public void setPayKind(String payKind) {
        this.payKind = payKind;
    }

    public String getCachierID() {
        return cachierID;
    }

    public void setCachierID(String cachierID) {
        this.cachierID = cachierID;
    }

    public String getTransCurrency() {
        return transCurrency;
    }

    public void setTransCurrency(String transCurrency) {
        this.transCurrency = transCurrency;
    }

    public String getCachierID() {
        return cachierID;
    }

    public void setCachierID(String cachierID) {
        this.cachierID = cachierID;
    }
}

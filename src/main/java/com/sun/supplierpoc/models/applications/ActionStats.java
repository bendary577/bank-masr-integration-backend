package com.sun.supplierpoc.models.applications;

import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

public class ActionStats {
    @Id
    private String id;
    @DBRef
    private User agent;
    private double chargeAmount;
    private double deductAmount;
    private double entranceAmount;
    private String accountId;

    public ActionStats(User agent, double chargeAmount, double deductAmount, double entranceAmount, String accountId) {
        this.agent = agent;
        this.chargeAmount = chargeAmount;
        this.deductAmount = deductAmount;
        this.entranceAmount = entranceAmount;
        this.accountId = accountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getAgent() {
        return agent;
    }

    public void setAgent(User agent) {
        this.agent = agent;
    }

    public double getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(double chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public double getDeductAmount() {
        return deductAmount;
    }

    public void setDeductAmount(double deductAmount) {
        this.deductAmount = deductAmount;
    }

    public double getEntranceAmount() {
        return entranceAmount;
    }

    public void setEntranceAmount(double entranceAmount) {
        this.entranceAmount = entranceAmount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}

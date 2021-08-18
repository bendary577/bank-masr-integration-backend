package com.sun.supplierpoc.models.applications;

import com.sun.supplierpoc.models.configurations.RevenueCenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    private String Id;
    private String appUserId;
    private double balance;
    private List<WalletHistory> walletHistory = new ArrayList<WalletHistory>();
    private List<RevenueCenter> revenueCenters = new ArrayList<>();

    public Wallet() {
    }

    public Wallet(double balance) {
        this.balance = balance;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<WalletHistory> getWalletHistory() {
        return walletHistory;
    }

    public void setWalletHistory(List<WalletHistory> walletHistory) {
        this.walletHistory = walletHistory;
    }
}

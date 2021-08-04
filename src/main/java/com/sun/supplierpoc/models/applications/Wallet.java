package com.sun.supplierpoc.models.applications;

public class Wallet {

    private String appUserId;

    private double balance;

    private WalletHistory walletHistory;

    public Wallet() {
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

    public WalletHistory getWalletHistory() {
        return walletHistory;
    }

    public void setWalletHistory(WalletHistory walletHistory) {
        this.walletHistory = walletHistory;
    }
}

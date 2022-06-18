package com.sun.supplierpoc.models.applications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L;

    private String Id;
    private String appUserId;
    private List<Balance> balance;
    private List<WalletHistory> walletHistory = new ArrayList<WalletHistory>();

    public Wallet() {
    }

    public Wallet(List<Balance> balance) {
        this.balance = balance;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public List<Balance> getBalance() {
        return balance;
    }

    public void setBalance(List<Balance> balance) {
        this.balance = balance;
    }

    public List<WalletHistory> getWalletHistory() {
        return walletHistory;
    }

    public void setWalletHistory(List<WalletHistory> walletHistory) {
        this.walletHistory = walletHistory;
    }
}

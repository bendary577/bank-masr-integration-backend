package com.sun.supplierpoc.models.simphony.response;

import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.opera.Transaction;

import java.util.List;

public class TransInRange {

    private List<Transactions> transactions;

    private double totalSpend;

    public TransInRange() {
    }

    public TransInRange(List<Transactions> transactions, double totalSpend) {
        this.transactions = transactions;
        this.totalSpend = totalSpend;
    }

    public List<Transactions> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transactions> transactions) {
        this.transactions = transactions;
    }

    public double getTotalSpend() {
        return totalSpend;
    }

    public void setTotalSpend(double totalSpend) {
        this.totalSpend = totalSpend;
    }
}

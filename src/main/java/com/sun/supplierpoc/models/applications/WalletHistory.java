package com.sun.supplierpoc.models.applications;

public class WalletHistory {

    private String operation;

    private double amount;

    private double previousBalance;

    private double newBalance;

    public WalletHistory() {
    }

    public WalletHistory(String operation, double amount, double previousBalance, double newBalance) {
        this.operation = operation;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(double previousBalance) {
        this.previousBalance = previousBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(double newBalance) {
        this.newBalance = newBalance;
    }
}

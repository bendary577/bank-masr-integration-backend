package com.sun.supplierpoc.models.applications;

import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

public class WalletHistory {

    private String operation;
    private double amount;
    private double previousBalance;
    private double newBalance;
    private String check;
    private String employee;
    @DBRef
    private User user;
    private Date date;

    public WalletHistory() {
    }

    public WalletHistory(String operation, double amount, double previousBalance, double newBalance, User user, Date date) {
        this.operation = operation;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.user = user;
        this.date = date;

    }

//    public WalletHistory(String operation, double amount, double previousBalance, double newBalance, Date date) {
//        this.operation = operation;
//        this.amount = amount;
//        this.previousBalance = previousBalance;
//        this.newBalance = newBalance;
//        this.date = date ;
//    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }
}

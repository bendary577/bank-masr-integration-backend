package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class OverGroup implements Serializable {

    @Id
    private String id;
    private boolean checked;
    private String overGroup;
    private String wasteAccountCredit;
    private String wasteAccountDebit;
    private String inventoryAccount;
    private String expensesAccount;
    private String product;

    public OverGroup() {
        this.checked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getOverGroup() {
        return overGroup;
    }

    public void setOverGroup(String overGroup) {
        this.overGroup = overGroup;
    }

    public String getWasteAccountCredit() {
        return wasteAccountCredit;
    }

    public void setWasteAccountCredit(String wasteAccountCredit) {
        this.wasteAccountCredit = wasteAccountCredit;
    }

    public String getWasteAccountDebit() {
        return wasteAccountDebit;
    }

    public void setWasteAccountDebit(String wasteAccountDebit) {
        this.wasteAccountDebit = wasteAccountDebit;
    }

    public String getInventoryAccount() {
        return inventoryAccount;
    }

    public void setInventoryAccount(String inventoryAccount) {
        this.inventoryAccount = inventoryAccount;
    }

    public String getExpensesAccount() {
        return expensesAccount;
    }

    public void setExpensesAccount(String expensesAccount) {
        this.expensesAccount = expensesAccount;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}

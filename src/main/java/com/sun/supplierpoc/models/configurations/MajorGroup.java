package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;
import java.util.List;

public class MajorGroup {
    private boolean checked;
    private String overGroup="";
    private String majorGroup="";
    private String account="";
    private String discountAccount="";

    /*
    * Use it to merge multiple major groups
    * */
    private ArrayList<String> children = new ArrayList<>();
    private ArrayList<FamilyGroup> familyGroups = new ArrayList<>();
    private ArrayList<RevenueCenter> revenueCenters = new ArrayList<>();


    public MajorGroup() {
        this.checked = false;
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

    public String getMajorGroup() {
        return majorGroup;
    }

    public void setMajorGroup(String majorGroup) {
        this.majorGroup = majorGroup;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public List<String> getChildren() {
        return children;
    }

    public String getDiscountAccount() {
        return discountAccount;
    }

    public void setDiscountAccount(String discountAccount) {
        this.discountAccount = discountAccount;
    }

    public void setChildren(ArrayList<String> children) {
        this.children = children;
    }

    public ArrayList<FamilyGroup> getFamilyGroups() {
        return familyGroups;
    }

    public void setFamilyGroups(ArrayList<FamilyGroup> familyGroups) {
        this.familyGroups = familyGroups;
    }

    public ArrayList<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(ArrayList<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }
}

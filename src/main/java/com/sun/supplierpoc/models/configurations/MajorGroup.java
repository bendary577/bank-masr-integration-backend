package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class MajorGroup {
    @Id
    private String id;
    private boolean checked;
    private String overGroup="";
    private String majorGroup="";
    private String account="";
    private List<String> children = new ArrayList<>();

    public MajorGroup() {
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

    public void setChildren(List<String> children) {
        this.children = children;
    }
}

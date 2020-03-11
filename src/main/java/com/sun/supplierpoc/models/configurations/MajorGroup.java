package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

public class MajorGroup {
    @Id
    private String id;
    private boolean checked;
    private String overGroup;
    private String majorGroup;

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
}

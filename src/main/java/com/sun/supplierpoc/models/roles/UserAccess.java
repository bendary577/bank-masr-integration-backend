package com.sun.supplierpoc.models.roles;

import java.util.ArrayList;

public class UserAccess {
    private String groupName;
    private ArrayList<String> roles;

    public UserAccess() {
    }

    public UserAccess(String groupName, ArrayList<String> roles) {
        this.groupName = groupName;
        this.roles = roles;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getRoles() {
        return roles;
    }

    public void setRoles(ArrayList<String> roles) {
        this.roles = roles;
    }
}

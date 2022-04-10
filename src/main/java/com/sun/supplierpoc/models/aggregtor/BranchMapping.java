package com.sun.supplierpoc.models.aggregtor;

public class BranchMapping {

    private String name = "";
    private String foodIcsBranchId = "";
    private String talabatBranchId = "";

    /* Talabat branch admin credentials */
    private String username = "";
    private String password = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoodIcsBranchId() {
        return foodIcsBranchId;
    }

    public void setFoodIcsBranchId(String foodIcsBranchId) {
        this.foodIcsBranchId = foodIcsBranchId;
    }

    public String getTalabatBranchId() {
        return talabatBranchId;
    }

    public void setTalabatBranchId(String talabatBranchId) {
        this.talabatBranchId = talabatBranchId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package com.sun.supplierpoc.models.configurations;

public class CostCenter {
    public boolean checked;
    public String costCenter = "";
    public String department = "";
    public String project = "";
    public String future2 = "";
    public String company = "";
    public String businessUnit = "";
    public String account = "";
    public String product = "";
    public String interCompany = "";
    public String location = "";
    public String locationName = "";
    public String currency = "";
    public String costCenterReference = "";
    public String accountCode = "";

    public CostCenter() {
        this.checked = false;
    }

    public CostCenter(String locationName) {
        this.checked = false;
        this.locationName = locationName;
    }
}

package com.sun.supplierpoc.models;

public class CostCenter {
    public boolean checked;
    public String costCenter;
    public String department;
    public String project;
    public String future2;
    public String company;
    public String businessUnit;
    public String account;
    public String product;
    public String interCompany;
    public String location;
    public String currency;

    public CostCenter() {
    }

    public CostCenter(boolean checked, String costCenter, String department, String project, String future2,
                      String company, String businessUnit, String account, String product, String interCompany,
                      String location, String currency) {
        this.checked = checked;
        this.costCenter = costCenter;
        this.department = department;
        this.project = project;
        this.future2 = future2;
        this.company = company;
        this.businessUnit = businessUnit;
        this.account = account;
        this.product = product;
        this.interCompany = interCompany;
        this.location = location;
        this.currency = currency;
    }
}

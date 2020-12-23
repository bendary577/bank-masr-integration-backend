package com.sun.supplierpoc.models.simphony;


public class ppCheckFilter {

    private  String VendorCode ;

    private  Number EmployeeObjectNum;

    private  Number RvcObjectNum;

    private  String OrderTypeID;

    private  String LookUpStartDate;

    private  Boolean IncludeClosedCheck;

    private com.sun.supplierpoc.models.simphony.CheckNumbers CheckNumbers;
    public String getVendorCode() {
        return VendorCode;
    }

    public void setVendorCode(String vendorCode) {
        VendorCode = vendorCode;
    }

    public Number getEmployeeObjectNum() {
        return EmployeeObjectNum;
    }

    public void setEmployeeObjectNum(Number employeeObjectNum) {
        EmployeeObjectNum = employeeObjectNum;
    }

    public Number getRvcObjectNum() {
        return RvcObjectNum;
    }

    public void setRvcObjectNum(Number rvcObjectNum) {
        RvcObjectNum = rvcObjectNum;
    }

    public String getOrderTypeID() {
        return OrderTypeID;
    }

    public void setOrderTypeID(String orderTypeID) {
        OrderTypeID = orderTypeID;
    }

    public String getLookUpStartDate() {
        return LookUpStartDate;
    }

    public void setLookUpStartDate(String lookUpStartDate) {
        LookUpStartDate = lookUpStartDate;
    }

    public Boolean getIncludeClosedCheck() {
        return IncludeClosedCheck;
    }

    public void setIncludeClosedCheck(Boolean includeClosedCheck) {
        IncludeClosedCheck = includeClosedCheck;
    }

    public CheckNumbers getCheckNumbers() {
        return CheckNumbers;
    }

    public void setCheckNumbers(CheckNumbers checkNumbers) {
        CheckNumbers = checkNumbers;
    }
}

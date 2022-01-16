package com.sun.supplierpoc.pdfExporters;

public class EmployeeRepository {

    private Integer empId;
    private String empName;
    private Double empSal;
    private String empDept;

    public EmployeeRepository() {
    }

    public EmployeeRepository(Integer empId, String empName, Double empSal, String empDept) {
        this.empId = empId;
        this.empName = empName;
        this.empSal = empSal;
        this.empDept = empDept;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public Double getEmpSal() {
        return empSal;
    }

    public void setEmpSal(Double empSal) {
        this.empSal = empSal;
    }

    public String getEmpDept() {
        return empDept;
    }

    public void setEmpDept(String empDept) {
        this.empDept = empDept;
    }
}

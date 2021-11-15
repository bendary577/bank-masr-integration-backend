package com.sun.supplierpoc.models.simphony.check;

import com.sun.supplierpoc.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
@Document(collection = "zealPayment")
public class ZealPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;
    private String code = "-";
    private String totalDue = "0";
    private String checkNumber = "0";
    private int employeeId;
    private String revenueCentreName;
    private int revenueCentreId;
    private String message = "-";
    private String status = Constants.FAILED;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(String totalDue) {
        this.totalDue = totalDue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getRevenueCentreName() {
        return revenueCentreName;
    }

    public void setRevenueCentreName(String revenueCentreName) {
        this.revenueCentreName = revenueCentreName;
    }

    public int getRevenueCentreId() {
        return revenueCentreId;
    }

    public void setRevenueCentreId(int revenueCentreId) {
        this.revenueCentreId = revenueCentreId;
    }

    @Override
    public String toString() {
        return "ZealPayment{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", totalDue='" + totalDue + '\'' +
                ", checkNumber='" + checkNumber + '\'' +
                ", employeeId=" + employeeId +
                ", revenueCentreName='" + revenueCentreName + '\'' +
                ", revenueCentreId=" + revenueCentreId +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

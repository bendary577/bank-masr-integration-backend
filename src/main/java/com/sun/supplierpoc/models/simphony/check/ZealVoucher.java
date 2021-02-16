package com.sun.supplierpoc.models.simphony.check;

import com.sun.supplierpoc.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zealVoucher")
public class ZealVoucher {

    @Id
    private int id;
    private String code = "-";
    private String itemId = "-";
    private int employeeId;
    private String revenueCentreName;
    private int revenueCentreId;
    private String message = "-";
    private String checkNumber = "-";
    private String status = Constants.FAILED;

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

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
}

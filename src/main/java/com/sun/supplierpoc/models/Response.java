package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.simphony.DbMenuItemClass;
import com.sun.supplierpoc.models.simphony.DbMenuItemDefinition;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentRes;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Response<T> {
    private boolean status;
    private String message;
    private ArrayList<HashMap<String, Object>> entries;
    private T data;

    private String requestbody;
    private String requestResponse;

    private List<WebElement> rows = new ArrayList<>();

    private SimphonyPaymentRes simphonyPaymentRes;
    public Response() {
    }

    public void setBadStatus(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<HashMap<String, Object>> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<HashMap<String, Object>> entries) {
        this.entries = entries;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getRequestbody() {
        return requestbody;
    }

    public void setRequestbody(String requestbody) {
        this.requestbody = requestbody;
    }

    public String getRequestResponse() {
        return requestResponse;
    }

    public void setRequestResponse(String requestResponse) {
        this.requestResponse = requestResponse;
    }

    public List<WebElement> getRows() {
        return rows;
    }

    public void setRows(List<WebElement> rows) {
        this.rows = rows;
    }

    public SimphonyPaymentRes getSimphonyPaymentRes() {
        return simphonyPaymentRes;
    }

    public void setSimphonyPaymentRes(SimphonyPaymentRes simphonyPaymentRes) {
        this.simphonyPaymentRes = simphonyPaymentRes;
    }
}

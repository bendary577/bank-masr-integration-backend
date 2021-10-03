package com.sun.supplierpoc.models.simphony.response;

import com.sun.supplierpoc.models.OperationData;
import com.sun.supplierpoc.models.simphony.Message;

import java.util.ArrayList;

public class ZealRedeemResponse {
    private int id;
    private Message message;
    private int code;
    private String menuItemId;
    private boolean status;
    private ArrayList<OperationData> addedOperationData = new ArrayList<>();

    public ZealRedeemResponse() {
    }

    public ZealRedeemResponse(Message message, int code, String menuItemId, boolean status) {
        this.message = message;
        this.code = code;
        this.menuItemId = menuItemId;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<OperationData> getAddedOperationData() {
        return addedOperationData;
    }

    public void setAddedOperationData(ArrayList<OperationData> addedOperationData) {
        this.addedOperationData = addedOperationData;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}


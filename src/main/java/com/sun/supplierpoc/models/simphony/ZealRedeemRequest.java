package com.sun.supplierpoc.models.simphony;

public class ZealRedeemRequest {

    private int id;
    private String uuid;

    public ZealRedeemRequest() {
    }

    public ZealRedeemRequest(String uuid) {
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

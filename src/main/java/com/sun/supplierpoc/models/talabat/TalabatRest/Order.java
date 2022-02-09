package com.sun.supplierpoc.models.talabat.TalabatRest;


import java.util.Date;
import java.util.List;

public class Order {

    private String identifier;
    private String orderId;
    private String globalVendorCode;
    private String vendorName;
    private Date orderTimestamp;
    private String total;
    private String isBillable;
    private Payment payment;
    private Delivery delivery;
    private List<Item> items = null;
    private Integer version;
    private Object changedAt;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getGlobalVendorCode() {
        return globalVendorCode;
    }

    public void setGlobalVendorCode(String globalVendorCode) {
        this.globalVendorCode = globalVendorCode;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Date getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(Date orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getIsBillable() {
        return isBillable;
    }

    public void setIsBillable(String isBillable) {
        this.isBillable = isBillable;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Object getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Object changedAt) {
        this.changedAt = changedAt;
    }

}
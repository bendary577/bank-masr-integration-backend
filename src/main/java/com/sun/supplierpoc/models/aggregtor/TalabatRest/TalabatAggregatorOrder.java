package com.sun.supplierpoc.models.aggregtor.TalabatRest;

import com.sun.supplierpoc.models.AggregatorOrder;

import java.util.ArrayList;
import java.util.List;

public class TalabatAggregatorOrder extends AggregatorOrder {

    List<RestOrder> orders = new ArrayList<>();

    private Order order;

    private List<OrderStatus> orderStatuses;

    private List<Object> previousVersions;

    private boolean status;

    private String message;

    public void setOrders(List<RestOrder> orders) {
        this.orders = orders;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderStatus> getOrderStatuses() {
        return orderStatuses;
    }

    public void setOrderStatuses(List<OrderStatus> orderStatuses) {
        this.orderStatuses = orderStatuses;
    }

    public List<Object> getPreviousVersions() {
        return previousVersions;
    }

    public void setPreviousVersions(List<Object> previousVersions) {
        this.previousVersions = previousVersions;
    }

    public List<RestOrder> getOrders() {
        return orders;
    }

    public boolean getStatus() {
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

}

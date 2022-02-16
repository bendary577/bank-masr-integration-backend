package com.sun.supplierpoc.models.configurations;

public class OrderTypeChannels implements Cloneable{

    private boolean checked;
    private String orderType;
    private String channel;
    private String netSales = "0";

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getNetSales() {
        return netSales;
    }

    public void setNetSales(String netSales) {
        this.netSales = netSales;
    }

    @Override
    public OrderTypeChannels clone() {
        try {
            OrderTypeChannels clone = (OrderTypeChannels) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

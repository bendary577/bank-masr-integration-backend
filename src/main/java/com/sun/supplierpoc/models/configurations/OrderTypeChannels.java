package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class OrderTypeChannels implements Cloneable{

    private boolean checked;
    private ArrayList<String> orderType;
    private String channel;
    private String channelCount;
    private int checkCount = 0;
    private double netSales = 0;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public ArrayList<String> getOrderType() {
        return orderType;
    }

    public void setOrderType(ArrayList<String> orderType) {
        this.orderType = orderType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(int checkCount) {
        this.checkCount = checkCount;
    }

    public double getNetSales() {
        return netSales;
    }

    public void setNetSales(double netSales) {
        this.netSales = netSales;
    }

    public String getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(String channelCount) {
        this.channelCount = channelCount;
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

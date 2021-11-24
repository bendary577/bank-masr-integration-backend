package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class BookingConfiguration {
    public String fileBaseName = "";
    public String fileExtension = "";

    public int vatRate = 0;
    public int municipalityTaxRate = 0;
    public int serviceChargeRate= 0;

    /* API Configuration */
    private String username = "";
    private String password = "";
    private String gatewayKey = "";
    private String url = "";
    private String channel = "";

    public ArrayList<String> neglectedGroupCodes = new ArrayList<>();
    public ArrayList<String> neglectedRoomTypes = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGatewayKey() {
        return gatewayKey;
    }

    public void setGatewayKey(String gatewayKey) {
        this.gatewayKey = gatewayKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}


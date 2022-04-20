package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;
import java.util.List;

public class SalesAPIConfig {
    public boolean taxIncluded = true;

    public String apiKey  = "";

    public String apiEndpoint = "";

    public String apiURL = "";

    public List<OrderTypeChannels> orderTypeChannels = new ArrayList<>();

    public ArrayList<SalesAPIStatistics> statistics = new ArrayList<>();

    public String getApiURL() {
        return apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
}

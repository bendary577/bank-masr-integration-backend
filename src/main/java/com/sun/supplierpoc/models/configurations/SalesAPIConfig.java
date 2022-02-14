package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;
import java.util.List;

public class SalesAPIConfig {

    public String apiKey  = "";

    public String apiEndpoint = "";

    public List<OrderTypeChannels> orderTypeChannels = new ArrayList<>();

    public ArrayList<SalesAPIStatistics> statistics = new ArrayList<>();

}

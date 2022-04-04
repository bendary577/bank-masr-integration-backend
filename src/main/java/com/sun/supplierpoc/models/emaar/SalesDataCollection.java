package com.sun.supplierpoc.models.emaar;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

public class SalesDataCollection{
    @JsonProperty("SalesInfo")
    public ArrayList<SalesInfo> salesInfo = new ArrayList<>();
}

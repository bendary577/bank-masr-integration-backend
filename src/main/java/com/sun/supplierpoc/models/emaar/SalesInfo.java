package com.sun.supplierpoc.models.emaar;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

public class SalesInfo{
    @JsonProperty("UnitNo")
    public String unitNo;
    @JsonProperty("LeaseCode")
    public String leaseCode;
    @JsonProperty("SalesDate")
    public String salesDate;
    @JsonProperty("TransactionCount")
    public String transactionCount;
    @JsonProperty("NetSales")
    public String netSales;
    @JsonProperty("FandBSplit")
    public String fandBSplit;
}




package com.sun.supplierpoc.models.util;

import java.io.Serializable;

public class SyncJobDataCSV implements Serializable {
    public String accountCode = "";
    public String amount = "";

//    public String toCostCenter = "";
//    public String toAccountCode = "";
    public String fromLocation = "";
    public String toLocation = "";
    public String description = "";

    public String accountingPeriod = "";
    public String transactionDate = "";
    public String transactionReference = "";

    public String DCMarker = "";
    public String recordType = "";
    public String journalType = "";

    public String conversionCode = "";
    public String conversionRate = "";

    public String analysisCode1 = "";
    public String analysisCode2 = "";
    public String analysisCode3 = "";
    public String analysisCode4 = "";
    public String analysisCode5 = "";
    public String analysisCode6 = "";
    public String analysisCode7 = "";
    public String analysisCode8 = "";
    public String analysisCode9 = "";
    public String analysisCode10 = "";

    public SyncJobDataCSV() {
    }
}

package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class InforConfiguration {
    public String businessUnit = "";
    public String journalType = "";
    public String currencyCode = "";
    public String postingType = "";
    public String suspenseAccount = "";

    public String taxAccountCode= "";
    public ArrayList<InforTax> taxes = new ArrayList<>();
//    public String taxAccountReference= "";
//    public String noTaxAccountReference= "";
}

package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.CostCenter;

import java.util.ArrayList;

public class ConsumptionJournal {
    public String overGroup;
    public float totalCost;
    public String accountCode;
    public CostCenter costCenter;
    public String DCMarker = "";

    public ConsumptionJournal() { }

    // Consumption Controller
    public ConsumptionJournal(String overGroup, float totalCost, String accountCode,CostCenter costCenter, String DCMarker) {
        this.overGroup = overGroup;
        this.totalCost = totalCost;
        this.accountCode = accountCode;
        this.costCenter = costCenter;
        this.DCMarker = DCMarker;
    }

    public ArrayList<ConsumptionJournal> checkJournalExistence(ArrayList<ConsumptionJournal> journals, String overGroup, float cost,
                                             String accountCode, CostCenter costCenter, String DCMarker) {

        for (ConsumptionJournal journal : journals) {
            if (journal.overGroup.equals(overGroup)) {
                if(journal.costCenter != null && journal.costCenter.costCenter.equals(costCenter.costCenter)){
                    journal.totalCost += cost;
                    return journals;
                }
            }
        }

        journals.add(new ConsumptionJournal(overGroup, cost, accountCode, costCenter, DCMarker));
        return journals;
    }
}

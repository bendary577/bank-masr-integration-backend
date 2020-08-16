package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.Tax;
import com.sun.supplierpoc.models.configurations.Tender;

import java.util.ArrayList;
import java.util.HashMap;

public class Response {
    private boolean status;
    private String message;
    private ArrayList<HashMap<String, Object>> entries;

    // Sales Variables
    private ArrayList<Tax> salesTax = new ArrayList<>();
    private ArrayList<Tender> salesTender = new ArrayList<>();
    private ArrayList<Journal> salesMajorGroupGross = new ArrayList<>();
    private ArrayList<JournalBatch> journalBatches = new ArrayList<>();

    public Response() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<HashMap<String, Object>> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<HashMap<String, Object>> entries) {
        this.entries = entries;
    }

    public ArrayList<Tax> getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(ArrayList<Tax> salesTax) {
        this.salesTax = salesTax;
    }

    public ArrayList<Tender> getSalesTender() {
        return salesTender;
    }

    public void setSalesTender(ArrayList<Tender> salesTender) {
        this.salesTender = salesTender;
    }

    public ArrayList<Journal> getSalesMajorGroupGross() {
        return salesMajorGroupGross;
    }

    public void setSalesMajorGroupGross(ArrayList<Journal> salesMajorGroupGross) {
        this.salesMajorGroupGross = salesMajorGroupGross;
    }

    public ArrayList<JournalBatch> getJournalBatches() {
        return journalBatches;
    }

    public void setJournalBatches(ArrayList<JournalBatch> journalBatches) {
        this.journalBatches = journalBatches;
    }
}

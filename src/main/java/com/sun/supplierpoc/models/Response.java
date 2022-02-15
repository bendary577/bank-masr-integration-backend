package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.simphony.DbMenuItemClass;
import com.sun.supplierpoc.models.simphony.DbMenuItemDefinition;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentRes;
import com.sun.supplierpoc.models.talabat.FoodicsProduct;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Response<T> {
    private boolean status;
    private String message;
    private ArrayList<HashMap<String, Object>> entries;
    private T data;

    // Sales Variables
    private ArrayList<Tax> salesTax = new ArrayList<>();
    private ArrayList<Tender> salesTender = new ArrayList<>();
    private ArrayList<Discount> salesDiscount = new ArrayList<>();
    private ArrayList<Journal> salesMajorGroupGross = new ArrayList<>();
    private ArrayList<ServiceCharge> salesServiceCharge = new ArrayList<>();
    private ArrayList<DbMenuItemDefinition> menuItems = new ArrayList<>();
    private ArrayList<DbMenuItemClass> menuItemClasses = new ArrayList<>();
    private SalesStatistics salesStatistics = new SalesStatistics();
    private SalesAPIStatistics salesAPIStatistics = new SalesAPIStatistics();
    private ArrayList<JournalBatch> journalBatches = new ArrayList<>();


    // Supplier Variables
    private ArrayList<SyncJobData> addedSuppliers = new ArrayList<>();
    private ArrayList<SyncJobData> updatedSuppliers = new ArrayList<>();

    // Booked Production Variables
    private ArrayList<BookedProduction> bookedProduction = new ArrayList<>();

    private ArrayList<HashMap<String, Object>> waste = new ArrayList<>();

    private ArrayList<SyncJobData> addedSyncJobData = new ArrayList<>();
    private ArrayList<OperationData> addedOperationData = new ArrayList<>();

    private List<WebElement> rows = new ArrayList<>();
    private List<FoodicsProduct> foodicsProducts = new ArrayList<>();

    private SimphonyPaymentRes simphonyPaymentRes;
    public Response() {
    }

    public void setBadStatus(boolean status, String message) {
        this.status = status;
        this.message = message;
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

    public ArrayList<Discount> getSalesDiscount() {
        return salesDiscount;
    }

    public void setSalesDiscount(ArrayList<Discount> salesDiscount) {
        this.salesDiscount = salesDiscount;
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

    public ArrayList<ServiceCharge> getSalesServiceCharge() {
        return salesServiceCharge;
    }

    public void setSalesServiceCharge(ArrayList<ServiceCharge> salesServiceCharge) {
        this.salesServiceCharge = salesServiceCharge;
    }

    public ArrayList<DbMenuItemDefinition> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(ArrayList<DbMenuItemDefinition> menuItems) {
        this.menuItems = menuItems;
    }

    public SalesStatistics getSalesStatistics() {
        return salesStatistics;
    }

    public void setSalesStatistics(SalesStatistics salesStatistics) {
        this.salesStatistics = salesStatistics;
    }

    public ArrayList<SyncJobData> getAddedSuppliers() {
        return addedSuppliers;
    }

    public void setAddedSuppliers(ArrayList<SyncJobData> addedSuppliers) {
        this.addedSuppliers = addedSuppliers;
    }

    public ArrayList<SyncJobData> getUpdatedSuppliers() {
        return updatedSuppliers;
    }

    public void setUpdatedSuppliers(ArrayList<SyncJobData> updatedSuppliers) {
        this.updatedSuppliers = updatedSuppliers;
    }

    public ArrayList<BookedProduction> getBookedProduction() {
        return bookedProduction;
    }

    public void setBookedProduction(ArrayList<BookedProduction> bookedProduction) {
        this.bookedProduction = bookedProduction;
    }

    public ArrayList<HashMap<String, Object>> getWaste() {
        return waste;
    }

    public void setWaste(ArrayList<HashMap<String, Object>> waste) {
        this.waste = waste;
    }

    public ArrayList<SyncJobData> getAddedSyncJobData() {
        return addedSyncJobData;
    }

    public void setAddedSyncJobData(ArrayList<SyncJobData> addedSyncJobData) {
        this.addedSyncJobData = addedSyncJobData;
    }

    public ArrayList<OperationData> getAddedOperationData() {
        return addedOperationData;
    }

    public void setAddedOperationData(ArrayList<OperationData> addedOperationData) {
        this.addedOperationData = addedOperationData;
    }

    public ArrayList<DbMenuItemClass> getMenuItemClasses() {
        return menuItemClasses;
    }

    public void setMenuItemClasses(ArrayList<DbMenuItemClass> menuItemClasses) {
        this.menuItemClasses = menuItemClasses;
    }

    public List<WebElement> getRows() {
        return rows;
    }

    public void setRows(List<WebElement> rows) {
        this.rows = rows;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public SimphonyPaymentRes getSimphonyPaymentRes() {
        return simphonyPaymentRes;
    }

    public void setSimphonyPaymentRes(SimphonyPaymentRes simphonyPaymentRes) {
        this.simphonyPaymentRes = simphonyPaymentRes;
    }

    public SalesAPIStatistics getSalesAPIStatistics() {
        return salesAPIStatistics;
    }

    public void setSalesAPIStatistics(SalesAPIStatistics salesAPIStatistics) {
        this.salesAPIStatistics = salesAPIStatistics;
    }

    public List<FoodicsProduct> getFoodicsProducts() {
        return foodicsProducts;
    }

    public void setFoodicsProducts(List<FoodicsProduct> foodicsProducts) {
        this.foodicsProducts = foodicsProducts;
    }
}

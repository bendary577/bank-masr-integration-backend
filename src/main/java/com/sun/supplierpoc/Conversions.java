package com.sun.supplierpoc;

import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Conversions {
    public Conversions() {
    }

    public String transformColName(String columnName){
        return columnName.strip().toLowerCase().replace(' ', '_');
    }

    public Tender checkTenderExistence(ArrayList<Tender> tenders, String tenderName) {
        for (Tender tender : tenders) {
            if (tender.getTender().toLowerCase().equals(tenderName.toLowerCase())) {
                return tender;
            }
        }
        return new Tender();
    }

    public OverGroup checkOverGroupExistence(ArrayList<OverGroup> overGroups, String overGroupName) {
        for (OverGroup overGroup : overGroups) {
            if (overGroup.getOverGroup().toLowerCase().equals(overGroupName.toLowerCase())) {
                return overGroup;
            }
        }
        return new OverGroup();
    }

    public MajorGroup checkMajorGroupExistence(ArrayList<MajorGroup> majorGroups, String majorGroupName){
        for (MajorGroup majorGroup : majorGroups) {
            if (majorGroup.getMajorGroup().toLowerCase().equals(majorGroupName.toLowerCase())) {
                return majorGroup;
            }
        }

        return new MajorGroup();
    }

    public ItemGroup checkItemGroupExistence(ArrayList<ItemGroup> itemGroups, String itemGroupName){
        for (ItemGroup itemGroup : itemGroups) {
            if (itemGroup.getItemGroup().toLowerCase().equals(itemGroupName.toLowerCase())) {
                return itemGroup;
            }
        }
        return new ItemGroup();
    }

    public Item checkItemExistence(ArrayList<Item> items, String itemName){
        for (Item item : items) {
            if (item.getItem().toLowerCase().equals(itemName.toLowerCase())) {
                return item;
            }
        }
        return new Item();
    }

    public WasteGroup checkWasteTypeExistence(ArrayList<WasteGroup> wasteTypes, String wasteTypeName){
        for (WasteGroup wasteType : wasteTypes) {
            if (wasteType.getWasteGroup().toLowerCase().equals(wasteTypeName.toLowerCase())) {
                return wasteType;
            }
        }
        return new WasteGroup();
    }

    public CostCenter checkCostCenterExistence(ArrayList<CostCenter> costCenters, String costCenterName,
                                               boolean getOrUseFlag){
        for (CostCenter costCenter : costCenters) {
            String savedCostCenterName = costCenter.costCenter;
            if (!getOrUseFlag){ // True in case of getting and False in case of use
                if (savedCostCenterName.indexOf('(') != -1){
                    savedCostCenterName = savedCostCenterName.substring(0, savedCostCenterName.indexOf('(') - 1);
                }
            }
            if (savedCostCenterName.toLowerCase().equals(costCenterName.toLowerCase())) {
                return costCenter;
            }
        }
        return new CostCenter();
    }

    public SyncJobData checkSupplierExistence(ArrayList<SyncJobData> suppliers, String vendorName){
        for (SyncJobData supplier : suppliers) {
            if (supplier.getData().get("supplier").toLowerCase().equals(vendorName.toLowerCase())
            || supplier.getData().get("description").toLowerCase().equals(vendorName.toLowerCase())) {
                return supplier;
            }
        }
        return null;
    }

    public SyncJobData checkInvoiceExistence(ArrayList<SyncJobData> invoices, String invoiceNumber, String overGroup){
        for (SyncJobData invoice : invoices) {
            if (invoice.getData().get("invoiceNo").equals(invoiceNumber) &&
                    invoice.getData().get("overGroup").equals(overGroup)) {
                return invoice;
            }
        }
        return null;
    }

    public HashMap<String, Object> checkSunDefaultConfiguration(SyncJobType syncJobType){
        HashMap<String, Object> response = new HashMap<>();

        if (syncJobType.getConfiguration().getBusinessUnit().equals("")){
            String message = "Configure business unit before sync " + syncJobType.getName();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getJournalType().equals("")){
            String message = "Configure journal type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getCurrencyCode().equals("")){
            String message = "Configure currency code before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getPostingType().equals("")){
            String message = "Configure posting type before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getAnalysis().size() == 0){
            String message = "Configure analysis before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getLocationAnalysis().equals("")){
            String message = "Configure location ledger analysis before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        if (syncJobType.getConfiguration().getSuspenseAccount().equals("")){
            String message = "Configure suspense account before sync invoices " + syncJobType.getName().toLowerCase();
            response.put("message", message);
            response.put("success", false);
            return response;
        }

        return null;
    }

    public float convertStringToFloat(String value){
        value = value.toLowerCase().replaceAll(",", "");
        if (value.contains("(")){
            value = value.replace("(", "").replace(")", "");
            value = "-" + value;
        }
        return Math.round(Float.parseFloat(value));
    }
}

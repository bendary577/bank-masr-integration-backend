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

    public OverGroup checkOverGroupExistence(ArrayList<OverGroup> overGroups, String overGroupName) {
        for (OverGroup overGroup : overGroups) {
            if (overGroup.getOverGroup().equals(overGroupName)) {
                return overGroup;
            }
        }
        return new OverGroup();
    }

    public MajorGroup checkMajorGroupExistence(ArrayList<MajorGroup> majorGroups, String majorGroupName){
        for (MajorGroup majorGroup : majorGroups) {
            if (majorGroup.getMajorGroup().equals(majorGroupName)) {
                return majorGroup;
            }
        }

        return new MajorGroup();
    }

    public ItemGroup checkItemGroupExistence(ArrayList<ItemGroup> itemGroups, String itemGroupName){
        for (ItemGroup itemGroup : itemGroups) {
            if (itemGroup.getItemGroup().equals(itemGroupName)) {
                return itemGroup;
            }
        }
        return new ItemGroup();
    }

    public Item checkItemExistence(ArrayList<Item> items, String itemName){
        for (Item item : items) {
            if (item.getItem().equals(itemName)) {
                return item;
            }
        }
        return new Item();
    }

    public WasteGroup checkWasteTypeExistence(ArrayList<WasteGroup> wasteTypes, String wasteTypeName){
        for (WasteGroup wasteType : wasteTypes) {
            if (wasteType.getWasteGroup().equals(wasteTypeName)) {
                return wasteType;
            }
        }
        return new WasteGroup();
    }

    public CostCenter checkCostCenterExistence(ArrayList<CostCenter> costCenters, String costCenterName,
                                               boolean getOrUseFlag){
        for (CostCenter costCenter : costCenters) {
            String savedCostCenterName = costCenter.costCenter;
            if (!getOrUseFlag){ // True in case of getting and False in case od use
                if (savedCostCenterName.indexOf('(') != -1){
                    savedCostCenterName = savedCostCenterName.substring(0, savedCostCenterName.indexOf('(') - 1);
                }
            }
            if (savedCostCenterName.equals(costCenterName)) {
                return costCenter;
            }
        }
        return new CostCenter();
    }

    public SyncJobData checkSupplierExistence(ArrayList<SyncJobData> suppliers, String vendorName){
        for (SyncJobData supplier : suppliers) {
            if (supplier.getData().get("supplier").equals(vendorName)) {
                return supplier;
            }
        }
        return null;
    }

    public float convertStringToFloat(String value){
        value = value.toLowerCase().replaceAll(",", "").replace("(", "").replace(")", "");
        return Float.parseFloat(value);
    }
}

package com.sun.supplierpoc.models.configurations;

import java.util.ArrayList;

public class ConsumptionLocation {
    public boolean check = false;
    public CostCenter costCenter;
    public String accountCode = "";
    public ArrayList<ItemGroup> itemGroups = new ArrayList<>();
}

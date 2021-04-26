package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;

public class ConsumptionConfiguration implements Serializable {
    /*
     *  Consumption variables
     * get consumption based of (Location/Cost Center)
     * */
    public String consumptionBasedOnType = "";
    public ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    public ArrayList<ConsumptionLocation> consumptionLocations = new ArrayList<>();
    public ArrayList<ConsumptionLocation> consumptionCostCenter = new ArrayList<>();

}

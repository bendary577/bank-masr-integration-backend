package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;

public class ConsumptionConfiguration implements Serializable {
    /*
     *  Consumption variables
     * get consumption based of (Location/Cost Center)
     * */
    public String consumptionBasedOnType = "";

    // get consumption per (overGroup/itemGroup)
    public String consumptionPerGroup = "";
}

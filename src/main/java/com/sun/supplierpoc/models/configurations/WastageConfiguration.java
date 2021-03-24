package com.sun.supplierpoc.models.configurations;

import java.io.Serializable;
import java.util.ArrayList;

public class WastageConfiguration implements Serializable {
    public ArrayList<WasteGroup> wasteGroups = new ArrayList<>();
    public String wasteReport = "";
}

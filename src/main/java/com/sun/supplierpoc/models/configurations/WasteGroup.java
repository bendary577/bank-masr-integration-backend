package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class WasteGroup implements Serializable {
    @Id
    private String id;
    private boolean checked;
    private String wasteGroup="";

    public WasteGroup() {
        this.checked = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getWasteGroup() {
        return wasteGroup;
    }

    public void setWasteGroup(String wasteGroup) {
        this.wasteGroup = wasteGroup;
    }
}

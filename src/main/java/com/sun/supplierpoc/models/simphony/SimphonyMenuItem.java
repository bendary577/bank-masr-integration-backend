package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "MenuItem"
})
public class SimphonyMenuItem {
    @JsonProperty(value = "menuItem")
    private MenuItem MenuItem;

    public MenuItem getMenuItem() {
        return MenuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.MenuItem = menuItem;
    }
}

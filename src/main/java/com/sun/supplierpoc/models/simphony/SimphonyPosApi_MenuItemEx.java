package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "Condiments", "MenuItem"
})
public class SimphonyPosApi_MenuItemEx {

    @XmlElement(name = "Condiments")
    public Condiments Condiments;

    @XmlElement(name = "MenuItem")
    public MenuItem MenuItem;

    public com.sun.supplierpoc.models.simphony.Condiments getCondiments() {
        return Condiments;
    }

    @JsonProperty(value = "condiments")
    public void setCondiments(com.sun.supplierpoc.models.simphony.Condiments condiments) {
        Condiments = condiments;
    }

    public com.sun.supplierpoc.models.simphony.MenuItem getMenuItem() {
        return MenuItem;
    }

    @JsonProperty(value = "menuItems")
    public void setMenuItem(com.sun.supplierpoc.models.simphony.MenuItem menuItem) {
        MenuItem = menuItem;
    }
}

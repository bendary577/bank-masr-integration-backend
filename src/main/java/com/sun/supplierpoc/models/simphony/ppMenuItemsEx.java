package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "SimphonyPosApi_MenuItemEx"
})

public class ppMenuItemsEx {
    @XmlElementWrapper(name = "SimphonyPosApi_MenuItemEx")
    @XmlElement(name = "MenuItem")
    private List<MenuItem> SimphonyPosApi_MenuItemEx;

    public List<MenuItem> getSimphonyPosApi_MenuItemEx() {
        return SimphonyPosApi_MenuItemEx;
    }

    public void setSimphonyPosApi_MenuItemEx(List<MenuItem> SimphonyPosApi_MenuItemEx) {
        this.SimphonyPosApi_MenuItemEx = SimphonyPosApi_MenuItemEx;
    }
}




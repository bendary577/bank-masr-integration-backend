package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
public class ppMenuItemsEx {

    @XmlElement(name = "SimphonyPosApi_MenuItemEx")
    public List<SimphonyPosApi_MenuItemEx> SimphonyPosApi_MenuItemEx;

    public List<com.sun.supplierpoc.models.simphony.SimphonyPosApi_MenuItemEx> getSimphonyPosApi_MenuItemEx() {
        return SimphonyPosApi_MenuItemEx;
    }

    @JsonProperty(value = "simphony-menuItems")
    public void setSimphonyPosApi_MenuItemEx(List<com.sun.supplierpoc.models.simphony.SimphonyPosApi_MenuItemEx> simphonyPosApi_MenuItemEx) {
        SimphonyPosApi_MenuItemEx = simphonyPosApi_MenuItemEx;
    }
}




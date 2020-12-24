package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class ppMenuItemsEx {
    @XmlElementWrapper(name = "SimphonyPosApi_MenuItemEx")
    @XmlElement(name = "MenuItem")
    private List<MenuItem> SimphonyPosApi_MenuItemEx;

    public List<MenuItem> getSimphonyPosApi_MenuItemEx() {
        return SimphonyPosApi_MenuItemEx;
    }

    public void setSimphonyPosApi_MenuItemEx(List<MenuItem> simphonyPosApi_MenuItemEx) {
        SimphonyPosApi_MenuItemEx = simphonyPosApi_MenuItemEx;
    }
}


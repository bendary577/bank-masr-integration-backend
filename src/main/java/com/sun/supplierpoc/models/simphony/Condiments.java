package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Condiments {

    @XmlElement(name = "SimphonyPosApi_MenuItemDefinitionEx")
    public List<SimphonyPosApi_MenuItemDefinitionEx> SimphonyPosApi_MenuItemDefinitionEx;

    public List<com.sun.supplierpoc.models.simphony.SimphonyPosApi_MenuItemDefinitionEx> getSimphonyPosApi_MenuItemDefinitionEx() {
        return SimphonyPosApi_MenuItemDefinitionEx;
    }

    @JsonProperty(value = "condimentItems")
    public void setSimphonyPosApi_MenuItemDefinitionEx(List<com.sun.supplierpoc.models.simphony.SimphonyPosApi_MenuItemDefinitionEx> simphonyPosApi_MenuItemDefinitionEx) {
        SimphonyPosApi_MenuItemDefinitionEx = simphonyPosApi_MenuItemDefinitionEx;
    }
}
package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.*;
import java.util.List;
@XmlRootElement(name="Condiments")
@XmlAccessorType(XmlAccessType.FIELD)
public class Condiments {
    @XmlElementWrapper(name="SimphonyPosApi_MenuItemDefinition")

    private List<String> SimphonyPosApi_MenuItemDefinition;

    public List<String> getSimphonyPosApi_MenuItemDefinition() {
        return SimphonyPosApi_MenuItemDefinition;
    }

    public void setSimphonyPosApi_MenuItemDefinition(List<String> simphonyPosApi_MenuItemDefinition) {
        SimphonyPosApi_MenuItemDefinition = simphonyPosApi_MenuItemDefinition;
    }
}

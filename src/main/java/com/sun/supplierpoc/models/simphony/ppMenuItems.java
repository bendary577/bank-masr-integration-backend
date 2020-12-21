package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)
public class ppMenuItems {
    @XmlElementWrapper(name="SimphonyPosApi_MenuItem")
    private List<PosApi_MenuItem> SimphonyPosApi_MenuItem;

    public List<PosApi_MenuItem> getSimphonyPosApi_MenuItem() {
        return SimphonyPosApi_MenuItem;
    }

    public void setSimphonyPosApi_MenuItem(List<PosApi_MenuItem> simphonyPosApi_MenuItem) {
        SimphonyPosApi_MenuItem = simphonyPosApi_MenuItem;
    }
}

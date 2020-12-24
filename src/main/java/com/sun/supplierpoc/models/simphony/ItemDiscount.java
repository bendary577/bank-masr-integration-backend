package com.sun.supplierpoc.models.simphony;

import com.sun.supplierpoc.models.simphony.discount.SimphonyPosApi_DiscountEx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ItemDiscount")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemDiscount {
    @XmlElement(name = "SimphonyPosApi_DiscountEx")
    private SimphonyPosApi_DiscountEx SimphonyPosApi_DiscountEx;

    public SimphonyPosApi_DiscountEx getSimphonyPosApi_DiscountEx() {
        return SimphonyPosApi_DiscountEx;
    }

    public void setSimphonyPosApi_DiscountEx(SimphonyPosApi_DiscountEx simphonyPosApi_DiscountEx) {
        SimphonyPosApi_DiscountEx = simphonyPosApi_DiscountEx;
    }
}

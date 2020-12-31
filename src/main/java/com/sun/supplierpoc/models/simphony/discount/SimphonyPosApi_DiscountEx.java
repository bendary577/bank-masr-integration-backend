package com.sun.supplierpoc.models.simphony.discount;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "DiscObjectNum"
})
public class SimphonyPosApi_DiscountEx {
    @XmlElement(name = "DiscObjectNum")
    private String DiscObjectNum;

    public String getDiscObjectNum() {
        return DiscObjectNum;
    }

    public void setDiscObjectNum(String discObjectNum) {
        DiscObjectNum = discObjectNum;
    }
}

package com.sun.supplierpoc.models.simphony.discount;

import javax.xml.bind.annotation.XmlElement;

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

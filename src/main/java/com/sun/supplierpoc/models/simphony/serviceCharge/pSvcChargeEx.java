package com.sun.supplierpoc.models.simphony.serviceCharge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "SvcChgObjectNum"
})

public class pSvcChargeEx {
    private String SvcChgObjectNum;

    public String getSvcChgObjectNum() {
        return SvcChgObjectNum;
    }

    public void setSvcChgObjectNum(String svcChgObjectNum) {
        SvcChgObjectNum = svcChgObjectNum;
    }
}

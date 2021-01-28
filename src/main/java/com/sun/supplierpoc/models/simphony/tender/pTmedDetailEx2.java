package com.sun.supplierpoc.models.simphony.tender;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = {
//        "SimphonyPosApi_TmedDetailItemEx2"
//})
public class pTmedDetailEx2 {
    @XmlElement(name = "SimphonyPosApi_TmedDetailItemEx2")
    private SimphonyPosApi_TmedDetailItemEx2 tenderDetailItem;

    public SimphonyPosApi_TmedDetailItemEx2 getTenderDetailItem() {
        return tenderDetailItem;
    }

    public void setTenderDetailItem(SimphonyPosApi_TmedDetailItemEx2 tenderDetailItem) {
        this.tenderDetailItem = tenderDetailItem;
    }
}

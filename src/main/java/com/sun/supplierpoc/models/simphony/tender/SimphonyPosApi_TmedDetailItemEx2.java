package com.sun.supplierpoc.models.simphony.tender;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)

public class SimphonyPosApi_TmedDetailItemEx2 {
//    @XmlElement(name = "TmedEPayment")
//    private TmedEPayment TmedEPayment;
    @XmlElement(name = "TmedObjectNum")
    private String tenderNum;
    @XmlElement(name = "TmedPartialPayment")
    private String tenderPartialPayment;
//    @XmlElement(name = "TmedReference")
//    private String TmedReference;


    public String getTenderNum() {
        return tenderNum;
    }

    public void setTenderNum(String tenderNum) {
        this.tenderNum = tenderNum;
    }

    public String getTenderPartialPayment() {
        return tenderPartialPayment;
    }

    public void setTenderPartialPayment(String tenderPartialPayment) {
        this.tenderPartialPayment = tenderPartialPayment;
    }
}

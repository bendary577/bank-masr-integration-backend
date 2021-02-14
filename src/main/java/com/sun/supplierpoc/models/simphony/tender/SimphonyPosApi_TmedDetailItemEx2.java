package com.sun.supplierpoc.models.simphony.tender;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "TmedEPayment",
        "TmedObjectNum",
        "TmedPartialPayment",
        "TmedReference"
})
public class SimphonyPosApi_TmedDetailItemEx2 {
    @XmlElement(name = "TmedEPayment")
    private TmedEPayment TmedEPayment;
    @XmlElement(name = "TmedObjectNum")
    private String TmedObjectNum;
    @XmlElement(name = "TmedPartialPayment")
    private String TmedPartialPayment;
    @XmlElement(name = "TmedReference")
    private String TmedReference;

    public TmedEPayment getTmedEPayment() {
        return TmedEPayment;
    }

    public void setTmedEPayment(TmedEPayment tmedEPayment) {
        TmedEPayment = tmedEPayment;
    }

    public String getTmedObjectNum() {
        return TmedObjectNum;
    }

    public void setTmedObjectNum(String tmedObjectNum) {
        TmedObjectNum = tmedObjectNum;
    }

    public String getTmedPartialPayment() {
        return TmedPartialPayment;
    }

    public void setTmedPartialPayment(String tmedPartialPayment) {
        TmedPartialPayment = tmedPartialPayment;
    }

    public String getTmedReference() {
        return TmedReference;
    }

    public void setTmedReference(String tmedReference) {
        TmedReference = tmedReference;
    }
}

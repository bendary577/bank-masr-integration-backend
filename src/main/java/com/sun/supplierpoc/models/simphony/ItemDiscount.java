package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ItemDiscount")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemDiscount {
    @XmlElement(name = "discAmountOrPercent")
    private String discAmountOrPercent;
    @XmlElement(name = "discObjectNum")
    private String discObjectNum;
    @XmlElement(name = "discReference")
    private String discReference;

}

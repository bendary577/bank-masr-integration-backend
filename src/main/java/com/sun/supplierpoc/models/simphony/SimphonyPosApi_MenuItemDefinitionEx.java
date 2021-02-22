package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class SimphonyPosApi_MenuItemDefinitionEx {

    @XmlElement(name = "ItemDiscount")
    public ItemDiscount ItemDiscount;
    @XmlElement(name = "MiObjectNum")
    public String MiObjectNum;
    @XmlElement(name = "MiOverridePrice")
    public Object MiOverridePrice;
    @XmlElement(name = "MiQuantity")
    public int MiQuantity;
    @XmlElement(name = "MiReference")
    public String MiReference;
    @XmlElement(name = "MiWeight")
    public String MiWeight;
    @XmlElement(name = "MiMenuLevel")
    public String MiMenuLevel;
    @XmlElement(name = "MiSubLevel")
    public String MiSubLevel;
    @XmlElement(name = "MiPriceLevel")
    public String MiPriceLevel;
    @XmlElement(name = "MiDefinitionSeqNum")
    public String MiDefinitionSeqNum;

    public Object getItemDiscount() {
        return ItemDiscount;
    }

    public void setItemDiscount(com.sun.supplierpoc.models.simphony.ItemDiscount itemDiscount) {
        ItemDiscount = itemDiscount;
    }

    public String getMiObjectNum() {
        return MiObjectNum;
    }

    @JsonProperty(value = "id")
    public void setMiObjectNum(String miObjectNum) {
        MiObjectNum = miObjectNum;
    }

    public Object getMiOverridePrice() {
        return MiOverridePrice;
    }

    public void setMiOverridePrice(Object miOverridePrice) {
        MiOverridePrice = miOverridePrice;
    }

    public int getMiQuantity() {
        return MiQuantity;
    }

    @JsonProperty(value = "quantity")
    public void setMiQuantity(int miQuantity) {
        MiQuantity = miQuantity;
    }

    public String getMiReference() {
        return MiReference;
    }

    public void setMiReference(String miReference) {
        MiReference = miReference;
    }

    public String getMiWeight() {
        return MiWeight;
    }

    public void setMiWeight(String miWeight) {
        MiWeight = miWeight;
    }

    public String getMiMenuLevel() {
        return MiMenuLevel;
    }

    public void setMiMenuLevel(String miMenuLevel) {
        MiMenuLevel = miMenuLevel;
    }

    public String getMiSubLevel() {
        return MiSubLevel;
    }

    public void setMiSubLevel(String miSubLevel) {
        MiSubLevel = miSubLevel;
    }

    public String getMiPriceLevel() {
        return MiPriceLevel;
    }

    public void setMiPriceLevel(String miPriceLevel) {
        MiPriceLevel = miPriceLevel;
    }

    public String getMiDefinitionSeqNum() {
        return MiDefinitionSeqNum;
    }

    public void setMiDefinitionSeqNum(String miDefinitionSeqNum) {
        MiDefinitionSeqNum = miDefinitionSeqNum;
    }
}
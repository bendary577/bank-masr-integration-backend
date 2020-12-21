package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
@XmlAccessorType(XmlAccessType.FIELD)
public class PosApi_MenuItemDefinition {
    @XmlElement(name = "ItemDiscount")
    private ItemDiscount itemDiscount;
    @XmlElement(name = "MiObjectNum")
    private Number miObjectNum;
    @XmlElement(name = "MiOverridePrice")
    private String miOverridePrice;
    @XmlElement(name = "MiReference")
    private String miReference;
    @XmlElement(name = "MiWeight")
    private String miWeight;
    @XmlElement(name = "MiMenuLevel")
    private Number miMenuLevel;
    @XmlElement(name = "MiSubLevel")
    private Number miSubLevel;
    @XmlElement(name = "MiPriveLevel")
    private Number miPriveLevel;

    public ItemDiscount getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(ItemDiscount itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public Number getMiObjectNum() {
        return miObjectNum;
    }

    public void setMiObjectNum(Number miObjectNum) {
        this.miObjectNum = miObjectNum;
    }

    public String getMiOverridePrice() {
        return miOverridePrice;
    }

    public void setMiOverridePrice(String miOverridePrice) {
        this.miOverridePrice = miOverridePrice;
    }

    public String getMiReference() {
        return miReference;
    }

    public void setMiReference(String miReference) {
        this.miReference = miReference;
    }

    public String getMiWeight() {
        return miWeight;
    }

    public void setMiWeight(String miWeight) {
        this.miWeight = miWeight;
    }

    public Number getMiMenuLevel() {
        return miMenuLevel;
    }

    public void setMiMenuLevel(Number miMenuLevel) {
        this.miMenuLevel = miMenuLevel;
    }

    public Number getMiSubLevel() {
        return miSubLevel;
    }

    public void setMiSubLevel(Number miSubLevel) {
        this.miSubLevel = miSubLevel;
    }

    public Number getMiPriveLevel() {
        return miPriveLevel;
    }

    public void setMiPriveLevel(Number miPriveLevel) {
        this.miPriveLevel = miPriveLevel;
    }
}

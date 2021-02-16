package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class MenuItem {
    @XmlElement(name = "ItemDiscount")
    private ItemDiscount itemDiscount;

    /* Object number of given menu item */
    @XmlElement(name = "MiObjectNum")
    private String miObjectNum;

    /* This must be a value between 1 and 8 */
    @XmlElement(name = "MiMenuLevel")
    private String miMenuLevel;

    /* This must be a value between 1 and 8 */
    @XmlElement(name = "MiSubLevel")
    private String miSubLevel;

    /* This is not currently supported */
    @XmlElement(name = "MiPriceLevel")
    private String MiPriceLevel;

    @XmlElement(name = "MiDefinitionSeqNum")
    private String MiDefinitionSeqNum;

    @XmlElement(name = "MiQuantity")
    private String MiQuantity;

    @XmlElement(name = "MiOverridePrice")
    private String miOverridePrice;
    @XmlElement(name = "MiReference")
    private String miReference;
    @XmlElement(name = "MiWeight")
    private String miWeight;

    public ItemDiscount getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(ItemDiscount itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    @JsonProperty(value = "id")
    public String getMiObjectNum() {
        return miObjectNum;
    }

    public void setMiObjectNum(String miObjectNum) {
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

    public String getMiMenuLevel() {
        return miMenuLevel;
    }

    public void setMiMenuLevel(String miMenuLevel) {
        this.miMenuLevel = miMenuLevel;
    }

    public String getMiSubLevel() {
        return miSubLevel;
    }

    public void setMiSubLevel(String miSubLevel) {
        this.miSubLevel = miSubLevel;
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

    public String getMiQuantity() {
        return MiQuantity;
    }

    @JsonProperty(value = "quantity")
    public void setMiQuantity(String miQuantity) {
        MiQuantity = miQuantity;
    }
}

package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;

public class DbMenuItemPrice {
    @XmlElement(name = "RecipeNameID")
    private String RecipeNameID ;
    @XmlElement(name = "ParentTaxClassOvrdObjNmbr")
    private String  ParentTaxClassOvrdObjNmbr ;
    @XmlElement(name = "HierStrucID")
    private String HierStrucID ;
    @XmlElement(name = "PosRef")
    private String PosRef ;
    @XmlElement(name = "MenuItemDefID")
    private String MenuItemDefID ;
    @XmlElement(name = "SequenceNum")
    private String SequenceNum ;
    @XmlElement(name = "PriceGroupID")
    private String PriceGroupID ;
    @XmlElement(name = "PrepCost")
    private String PrepCost ;
    @XmlElement(name = "TaxClassObjNum")
    private String TaxClassObjNum ;
    @XmlElement(name = "OptionBits")
    private String OptionBits ;
    @XmlElement(name = "ServiceChargeGroupObjNum")
    private String ServiceChargeGroupObjNum ;
    @XmlElement(name = "MenuLvlIndex")
    private String MenuLvlIndex ;
    @XmlElement(name = "Price")
    private String Price ;
    @XmlElement(name = "ChangeSetObjNum")
    private String ChangeSetObjNum ;
    @XmlElement(name = "MenuItemPriceID")
    private String MenuItemPriceID ;

    public String getRecipeNameID() {
        return RecipeNameID;
    }

    public void setRecipeNameID(String recipeNameID) {
        RecipeNameID = recipeNameID;
    }

    public String getParentTaxClassOvrdObjNmbr() {
        return ParentTaxClassOvrdObjNmbr;
    }

    public void setParentTaxClassOvrdObjNmbr(String parentTaxClassOvrdObjNmbr) {
        ParentTaxClassOvrdObjNmbr = parentTaxClassOvrdObjNmbr;
    }



    public String getHierStrucID() {
        return HierStrucID;
    }

    public void setHierStrucID(String hierStrucID) {
        HierStrucID = hierStrucID;
    }

    public String getPosRef() {
        return PosRef;
    }

    public void setPosRef(String posRef) {
        PosRef = posRef;
    }

    public String getMenuItemDefID() {
        return MenuItemDefID;
    }

    public void setMenuItemDefID(String menuItemDefID) {
        MenuItemDefID = menuItemDefID;
    }

    public String getSequenceNum() {
        return SequenceNum;
    }

    public void setSequenceNum(String sequenceNum) {
        SequenceNum = sequenceNum;
    }

    public String getPriceGroupID() {
        return PriceGroupID;
    }

    public void setPriceGroupID(String priceGroupID) {
        PriceGroupID = priceGroupID;
    }

    public String getPrepCost() {
        return PrepCost;
    }

    public void setPrepCost(String prepCost) {
        PrepCost = prepCost;
    }

    public String getTaxClassObjNum() {
        return TaxClassObjNum;
    }

    public void setTaxClassObjNum(String taxClassObjNum) {
        TaxClassObjNum = taxClassObjNum;
    }

    public String getOptionBits() {
        return OptionBits;
    }

    public void setOptionBits(String optionBits) {
        OptionBits = optionBits;
    }

    public String getServiceChargeGroupObjNum() {
        return ServiceChargeGroupObjNum;
    }

    public void setServiceChargeGroupObjNum(String serviceChargeGroupObjNum) {
        ServiceChargeGroupObjNum = serviceChargeGroupObjNum;
    }

    public String getMenuLvlIndex() {
        return MenuLvlIndex;
    }

    public void setMenuLvlIndex(String menuLvlIndex) {
        MenuLvlIndex = menuLvlIndex;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getChangeSetObjNum() {
        return ChangeSetObjNum;
    }

    public void setChangeSetObjNum(String changeSetObjNum) {
        ChangeSetObjNum = changeSetObjNum;
    }

    public String getMenuItemPriceID() {
        return MenuItemPriceID;
    }

    public void setMenuItemPriceID(String menuItemPriceID) {
        MenuItemPriceID = menuItemPriceID;
    }
}

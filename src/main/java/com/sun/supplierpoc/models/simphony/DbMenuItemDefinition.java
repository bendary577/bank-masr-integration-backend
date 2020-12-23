package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;

public class DbMenuItemDefinition {
    @XmlElement(name = "NameOptions")
    private String NameOptions ;
    @XmlElement(name = "MenuItemDefID")
    private String MenuItemDefID ;
    @XmlElement(name = "HierStrucID")
    private String HierStrucID ;
    @XmlElement(name = "MenuItemMasterID")
    private String MenuItemMasterID ;
    @XmlElement(name = "SequenceNum")
    private String SequenceNum ;
    @XmlElement(name = "SluSort")
    private String SluSort ;
    @XmlElement(name = "NluNumber")
    private String NluNumber ;
    @XmlElement(name = "Tare")
    private String Tare ;
    @XmlElement(name = "Surcharge")
    private String Surcharge ;
    @XmlElement(name = "IconNumber")
    private String IconNumber ;
    @XmlElement(name = "OptionBits")
    private String OptionBits ;
    @XmlElement(name = "SpecialCount")
    private String SpecialCount ;
    @XmlElement(name = "PrepTime")
    private String PrepTime ;
    @XmlElement(name = "MenuItemClassObjNum")
    private String MenuItemClassObjNum;
    @XmlElement(name = "NluGroupIndex")
    private String NluGroupIndex ;
    @XmlElement(name = "SluIndex")
    private String SluIndex ;
    @XmlElement(name = "HhtSluIndex")
    private String HhtSluIndex ;
    @XmlElement(name = "MainLevels")
    private String MainLevels ;
    @XmlElement(name = "SubLevels")
    private String SubLevels ;
    @XmlElement(name = "PosRef")
    private String PosRef ;
    @XmlElement(name = "PrintClassObjNum")
    private String PrintClassObjNum ;
    @XmlElement(name = "PrefixLevelOverride")
    private String PrefixLevelOverride;
    @XmlElement(name = "GuestCount")
    private String GuestCount ;
    @XmlElement(name = "Quantity")
    private String Quantity ;
    @XmlElement(name = "MenuLevelEntries")
    private String MenuLevelEntries ;
    @XmlElement(name = "DefaultCondiments")
    private String DefaultCondiments ;
    @XmlElement(name = "NextScreen")
    private String NextScreen ;
    @XmlElement(name = "MiMasterObjNum")
    private String MiMasterObjNum ;
    @XmlElement(name = "CheckAvailability")
    private Boolean CheckAvailability ;
    @XmlElement(name = "OutOfMenuItem")
    private Boolean OutOfMenuItem ;

    private DbMenuItemPrice menuItemPrice;

    public String getNameOptions() {
        return NameOptions;
    }

    public void setNameOptions(String nameOptions) {
        NameOptions = nameOptions;
    }

    public String getMenuItemDefID() {
        return MenuItemDefID;
    }

    public void setMenuItemDefID(String menuItemDefID) {
        MenuItemDefID = menuItemDefID;
    }

    public String getHierStrucID() {
        return HierStrucID;
    }

    public void setHierStrucID(String hierStrucID) {
        HierStrucID = hierStrucID;
    }

    public String getMenuItemMasterID() {
        return MenuItemMasterID;
    }

    public void setMenuItemMasterID(String menuItemMasterID) {
        MenuItemMasterID = menuItemMasterID;
    }

    public String getSequenceNum() {
        return SequenceNum;
    }

    public void setSequenceNum(String sequenceNum) {
        SequenceNum = sequenceNum;
    }

    public String getSluSort() {
        return SluSort;
    }

    public void setSluSort(String sluSort) {
        SluSort = sluSort;
    }

    public String getNluNumber() {
        return NluNumber;
    }

    public void setNluNumber(String nluNumber) {
        NluNumber = nluNumber;
    }

    public String getTare() {
        return Tare;
    }

    public void setTare(String tare) {
        Tare = tare;
    }

    public String getSurcharge() {
        return Surcharge;
    }

    public void setSurcharge(String surcharge) {
        Surcharge = surcharge;
    }

    public String getIconNumber() {
        return IconNumber;
    }

    public void setIconNumber(String iconNumber) {
        IconNumber = iconNumber;
    }

    public String getOptionBits() {
        return OptionBits;
    }

    public void setOptionBits(String optionBits) {
        OptionBits = optionBits;
    }

    public String getSpecialCount() {
        return SpecialCount;
    }

    public void setSpecialCount(String specialCount) {
        SpecialCount = specialCount;
    }

    public String getPrepTime() {
        return PrepTime;
    }

    public void setPrepTime(String prepTime) {
        PrepTime = prepTime;
    }

    public String getMenuItemClassObjNum() {
        return MenuItemClassObjNum;
    }

    public void setMenuItemClassObjNum(String menuItemClassObjNum) {
        MenuItemClassObjNum = menuItemClassObjNum;
    }

    public String getNluGroupIndex() {
        return NluGroupIndex;
    }

    public void setNluGroupIndex(String nluGroupIndex) {
        NluGroupIndex = nluGroupIndex;
    }

    public String getSluIndex() {
        return SluIndex;
    }

    public void setSluIndex(String sluIndex) {
        SluIndex = sluIndex;
    }

    public String getHhtSluIndex() {
        return HhtSluIndex;
    }

    public void setHhtSluIndex(String hhtSluIndex) {
        HhtSluIndex = hhtSluIndex;
    }

    public String getMainLevels() {
        return MainLevels;
    }

    public void setMainLevels(String mainLevels) {
        MainLevels = mainLevels;
    }

    public String getSubLevels() {
        return SubLevels;
    }

    public void setSubLevels(String subLevels) {
        SubLevels = subLevels;
    }

    public String getPosRef() {
        return PosRef;
    }

    public void setPosRef(String posRef) {
        PosRef = posRef;
    }

    public String getPrintClassObjNum() {
        return PrintClassObjNum;
    }

    public void setPrintClassObjNum(String printClassObjNum) {
        PrintClassObjNum = printClassObjNum;
    }

    public String getPrefixLevelOverride() {
        return PrefixLevelOverride;
    }

    public void setPrefixLevelOverride(String prefixLevelOverride) {
        PrefixLevelOverride = prefixLevelOverride;
    }

    public String getGuestCount() {
        return GuestCount;
    }

    public void setGuestCount(String guestCount) {
        GuestCount = guestCount;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getMenuLevelEntries() {
        return MenuLevelEntries;
    }

    public void setMenuLevelEntries(String menuLevelEntries) {
        MenuLevelEntries = menuLevelEntries;
    }

    public String getDefaultCondiments() {
        return DefaultCondiments;
    }

    public void setDefaultCondiments(String defaultCondiments) {
        DefaultCondiments = defaultCondiments;
    }

    public String getNextScreen() {
        return NextScreen;
    }

    public void setNextScreen(String nextScreen) {
        NextScreen = nextScreen;
    }

    public String getMiMasterObjNum() {
        return MiMasterObjNum;
    }

    public void setMiMasterObjNum(String miMasterObjNum) {
        MiMasterObjNum = miMasterObjNum;
    }

    public Boolean getCheckAvailability() {
        return CheckAvailability;
    }

    public void setCheckAvailability(Boolean checkAvailability) {
        CheckAvailability = checkAvailability;
    }

    public Boolean getOutOfMenuItem() {
        return OutOfMenuItem;
    }

    public void setOutOfMenuItem(Boolean outOfMenuItem) {
        OutOfMenuItem = outOfMenuItem;
    }

    public DbMenuItemPrice getMenuItemPrice() {
        return menuItemPrice;
    }

    public void setMenuItemPrice(DbMenuItemPrice menuItemPrice) {
        this.menuItemPrice = menuItemPrice;
    }
}

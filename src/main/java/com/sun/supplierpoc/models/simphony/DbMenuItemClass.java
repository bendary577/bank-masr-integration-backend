package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;

public class DbMenuItemClass {
    @XmlElement(name = "MenuItemClassID")
    public int MenuItemClassID;
    @XmlElement(name = "HierStrucID")
    public int HierStrucID;
    @XmlElement(name = "ObjectNumber")
    public int ObjectNumber;
    @XmlElement(name = "Name")
    public Name Name;
    @XmlElement(name = "OptionBits")
    public double OptionBits;
    @XmlElement(name = "TransDfltMain")
    public boolean TransDfltMain;
    @XmlElement(name = "TransDfltSub")
    public boolean TransDfltSub;
    @XmlElement(name = "DatabasePrintOptionBits")
    public int DatabasePrintOptionBits;
    @XmlElement(name = "PrintGroup")
    public int PrintGroup;
    @XmlElement(name = "PrivilegeGroup")
    public int PrivilegeGroup;
    @XmlElement(name = "DscntItmzrIndex")
    public int DscntItmzrIndex;
    @XmlElement(name = "SvcChgItmzrIndex")
    public int SvcChgItmzrIndex;
    @XmlElement(name = "Halo")
    public int Halo;
    @XmlElement(name = "CondimentOrderingType")
    public String CondimentOrderingType;
    @XmlElement(name = "TaxClassObjNum")
    public int TaxClassObjNum;
    @XmlElement(name = "SlsItmzrIndex")
    public int SlsItmzrIndex;
    @XmlElement(name = "MainMenuLvlIndex")
    public int MainMenuLvlIndex;
    @XmlElement(name = "SubMenuLvlIndex")
    public int SubMenuLvlIndex;
    @XmlElement(name = "DefaultMasterGroupObjNum")
    public int DefaultMasterGroupObjNum;
    @XmlElement(name = "CountEntryType")
    public String CountEntryType;
    @XmlElement(name = "CountDisplayType")
    public String CountDisplayType;
    @XmlElement(name = "PricingCalculation")
    public String PricingCalculation;
    @XmlElement(name = "MemberOfCondiments")
    public double MemberOfCondiments;
    @XmlElement(name = "RequiredCondiments")
    public double RequiredCondiments;
    @XmlElement(name = "AllowedCondiments")
    public double AllowedCondiments;
    @XmlElement(name = "RoutingGroupObjNum")
    public int RoutingGroupObjNum;
    @XmlElement(name = "KdsCourseNum")
    public int KdsCourseNum;
    @XmlElement(name = "PrintClassObjNum")
    public int PrintClassObjNum;
    @XmlElement(name = "NextPage")
    public int NextPage;
    @XmlElement(name = "CondimentPrefixType")
    public int CondimentPrefixType;
    @XmlElement(name = "DefaultOrderTypeIndex")
    public int DefaultOrderTypeIndex;
    @XmlElement(name = "KdsHighlightSchemeObjNum")
    public int KdsHighlightSchemeObjNum;
    @XmlElement(name = "MaxRefillCount")
    public int MaxRefillCount;
    @XmlElement(name = "RefillDescriptor")
    public RefillDescriptor RefillDescriptor;
    @XmlElement(name = "PopupCondOrderPageObjNum")
    public int PopupCondOrderPageObjNum;
    @XmlElement(name = "PopupCondEditPageObjNum")
    public int PopupCondEditPageObjNum;
    @XmlElement(name = "ServiceChargeGroupObjNum")
    public int ServiceChargeGroupObjNum;
    @XmlElement(name = "PreProdChitPrintClassObjNum")
    public int PreProdChitPrintClassObjNum;
    @XmlElement(name = "CondimentHandling")
    public String CondimentHandling;

    public int getMenuItemClassID() {
        return MenuItemClassID;
    }

    public void setMenuItemClassID(int menuItemClassID) {
        MenuItemClassID = menuItemClassID;
    }

    public int getHierStrucID() {
        return HierStrucID;
    }

    public void setHierStrucID(int hierStrucID) {
        HierStrucID = hierStrucID;
    }

    public int getObjectNumber() {
        return ObjectNumber;
    }

    public void setObjectNumber(int objectNumber) {
        ObjectNumber = objectNumber;
    }

    public Name getName() {
        return Name;
    }

    public void setName(Name name) {
        Name = name;
    }

    public double getOptionBits() {
        return OptionBits;
    }

    public void setOptionBits(double optionBits) {
        OptionBits = optionBits;
    }

    public boolean isTransDfltMain() {
        return TransDfltMain;
    }

    public void setTransDfltMain(boolean transDfltMain) {
        TransDfltMain = transDfltMain;
    }

    public boolean isTransDfltSub() {
        return TransDfltSub;
    }

    public void setTransDfltSub(boolean transDfltSub) {
        TransDfltSub = transDfltSub;
    }

    public int getDatabasePrintOptionBits() {
        return DatabasePrintOptionBits;
    }

    public void setDatabasePrintOptionBits(int databasePrintOptionBits) {
        DatabasePrintOptionBits = databasePrintOptionBits;
    }

    public int getPrintGroup() {
        return PrintGroup;
    }

    public void setPrintGroup(int printGroup) {
        PrintGroup = printGroup;
    }

    public int getPrivilegeGroup() {
        return PrivilegeGroup;
    }

    public void setPrivilegeGroup(int privilegeGroup) {
        PrivilegeGroup = privilegeGroup;
    }

    public int getDscntItmzrIndex() {
        return DscntItmzrIndex;
    }

    public void setDscntItmzrIndex(int dscntItmzrIndex) {
        DscntItmzrIndex = dscntItmzrIndex;
    }

    public int getSvcChgItmzrIndex() {
        return SvcChgItmzrIndex;
    }

    public void setSvcChgItmzrIndex(int svcChgItmzrIndex) {
        SvcChgItmzrIndex = svcChgItmzrIndex;
    }

    public int getHalo() {
        return Halo;
    }

    public void setHalo(int halo) {
        Halo = halo;
    }

    public String getCondimentOrderingType() {
        return CondimentOrderingType;
    }

    public void setCondimentOrderingType(String condimentOrderingType) {
        CondimentOrderingType = condimentOrderingType;
    }

    public int getTaxClassObjNum() {
        return TaxClassObjNum;
    }

    public void setTaxClassObjNum(int taxClassObjNum) {
        TaxClassObjNum = taxClassObjNum;
    }

    public int getSlsItmzrIndex() {
        return SlsItmzrIndex;
    }

    public void setSlsItmzrIndex(int slsItmzrIndex) {
        SlsItmzrIndex = slsItmzrIndex;
    }

    public int getMainMenuLvlIndex() {
        return MainMenuLvlIndex;
    }

    public void setMainMenuLvlIndex(int mainMenuLvlIndex) {
        MainMenuLvlIndex = mainMenuLvlIndex;
    }

    public int getSubMenuLvlIndex() {
        return SubMenuLvlIndex;
    }

    public void setSubMenuLvlIndex(int subMenuLvlIndex) {
        SubMenuLvlIndex = subMenuLvlIndex;
    }

    public int getDefaultMasterGroupObjNum() {
        return DefaultMasterGroupObjNum;
    }

    public void setDefaultMasterGroupObjNum(int defaultMasterGroupObjNum) {
        DefaultMasterGroupObjNum = defaultMasterGroupObjNum;
    }

    public String getCountEntryType() {
        return CountEntryType;
    }

    public void setCountEntryType(String countEntryType) {
        CountEntryType = countEntryType;
    }

    public String getCountDisplayType() {
        return CountDisplayType;
    }

    public void setCountDisplayType(String countDisplayType) {
        CountDisplayType = countDisplayType;
    }

    public String getPricingCalculation() {
        return PricingCalculation;
    }

    public void setPricingCalculation(String pricingCalculation) {
        PricingCalculation = pricingCalculation;
    }

    public double getMemberOfCondiments() {
        return MemberOfCondiments;
    }

    public void setMemberOfCondiments(double memberOfCondiments) {
        MemberOfCondiments = memberOfCondiments;
    }

    public double getRequiredCondiments() {
        return RequiredCondiments;
    }

    public void setRequiredCondiments(double requiredCondiments) {
        RequiredCondiments = requiredCondiments;
    }

    public double getAllowedCondiments() {
        return AllowedCondiments;
    }

    public void setAllowedCondiments(double allowedCondiments) {
        AllowedCondiments = allowedCondiments;
    }

    public int getRoutingGroupObjNum() {
        return RoutingGroupObjNum;
    }

    public void setRoutingGroupObjNum(int routingGroupObjNum) {
        RoutingGroupObjNum = routingGroupObjNum;
    }

    public int getKdsCourseNum() {
        return KdsCourseNum;
    }

    public void setKdsCourseNum(int kdsCourseNum) {
        KdsCourseNum = kdsCourseNum;
    }

    public int getPrintClassObjNum() {
        return PrintClassObjNum;
    }

    public void setPrintClassObjNum(int printClassObjNum) {
        PrintClassObjNum = printClassObjNum;
    }

    public int getNextPage() {
        return NextPage;
    }

    public void setNextPage(int nextPage) {
        NextPage = nextPage;
    }

    public int getCondimentPrefixType() {
        return CondimentPrefixType;
    }

    public void setCondimentPrefixType(int condimentPrefixType) {
        CondimentPrefixType = condimentPrefixType;
    }

    public int getDefaultOrderTypeIndex() {
        return DefaultOrderTypeIndex;
    }

    public void setDefaultOrderTypeIndex(int defaultOrderTypeIndex) {
        DefaultOrderTypeIndex = defaultOrderTypeIndex;
    }

    public int getKdsHighlightSchemeObjNum() {
        return KdsHighlightSchemeObjNum;
    }

    public void setKdsHighlightSchemeObjNum(int kdsHighlightSchemeObjNum) {
        KdsHighlightSchemeObjNum = kdsHighlightSchemeObjNum;
    }

    public int getMaxRefillCount() {
        return MaxRefillCount;
    }

    public void setMaxRefillCount(int maxRefillCount) {
        MaxRefillCount = maxRefillCount;
    }

    public com.sun.supplierpoc.models.simphony.RefillDescriptor getRefillDescriptor() {
        return RefillDescriptor;
    }

    public void setRefillDescriptor(RefillDescriptor refillDescriptor) {
        RefillDescriptor = refillDescriptor;
    }

    public int getPopupCondOrderPageObjNum() {
        return PopupCondOrderPageObjNum;
    }

    public void setPopupCondOrderPageObjNum(int popupCondOrderPageObjNum) {
        PopupCondOrderPageObjNum = popupCondOrderPageObjNum;
    }

    public int getPopupCondEditPageObjNum() {
        return PopupCondEditPageObjNum;
    }

    public void setPopupCondEditPageObjNum(int popupCondEditPageObjNum) {
        PopupCondEditPageObjNum = popupCondEditPageObjNum;
    }

    public int getServiceChargeGroupObjNum() {
        return ServiceChargeGroupObjNum;
    }

    public void setServiceChargeGroupObjNum(int serviceChargeGroupObjNum) {
        ServiceChargeGroupObjNum = serviceChargeGroupObjNum;
    }

    public int getPreProdChitPrintClassObjNum() {
        return PreProdChitPrintClassObjNum;
    }

    public void setPreProdChitPrintClassObjNum(int preProdChitPrintClassObjNum) {
        PreProdChitPrintClassObjNum = preProdChitPrintClassObjNum;
    }

    public String getCondimentHandling() {
        return CondimentHandling;
    }

    public void setCondimentHandling(String condimentHandling) {
        CondimentHandling = condimentHandling;
    }

    @Override
    public String toString() {
        return "DbMenuItemClass{" +
                "MenuItemClassID=" + MenuItemClassID +
                ", HierStrucID=" + HierStrucID + '\'' +
                '}';
    }
}


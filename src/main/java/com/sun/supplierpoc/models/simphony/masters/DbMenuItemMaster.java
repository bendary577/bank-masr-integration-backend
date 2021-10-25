package com.sun.supplierpoc.models.simphony.masters;

import com.sun.supplierpoc.models.simphony.Name;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class DbMenuItemMaster {
    @XmlElement(name = "MenuItemMasterID")
    public int MenuItemMasterID;
    @XmlElement(name = "HierStrucID")
    public int HierStrucID;
    @XmlElement(name = "ObjectNumber")
    public int ObjectNumber;
    @XmlElement(name = "Name")
    public Name Name;
    @XmlElement(name = "ReportGroup")
    public int ReportGroup;
    @XmlElement(name = "Status")
    public int Status;
    @XmlElement(name = "RecipeLinkID")
    public int RecipeLinkID;
    @XmlElement(name = "FamGrpObjNum")
    public int FamGrpObjNum;
    @XmlElement(name = "MajGrpObjNum")
    public int MajGrpObjNum;
    @XmlElement(name = "MasterGrpObjNum")
    public int MasterGrpObjNum;


    public int getMenuItemMasterID() {
        return MenuItemMasterID;
    }

    public void setMenuItemMasterID(int menuItemMasterID) {
        MenuItemMasterID = menuItemMasterID;
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

    public int getReportGroup() {
        return ReportGroup;
    }

    public void setReportGroup(int reportGroup) {
        ReportGroup = reportGroup;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getRecipeLinkID() {
        return RecipeLinkID;
    }

    public void setRecipeLinkID(int recipeLinkID) {
        RecipeLinkID = recipeLinkID;
    }

    public int getFamGrpObjNum() {
        return FamGrpObjNum;
    }

    public void setFamGrpObjNum(int famGrpObjNum) {
        FamGrpObjNum = famGrpObjNum;
    }

    public int getMajGrpObjNum() {
        return MajGrpObjNum;
    }

    public void setMajGrpObjNum(int majGrpObjNum) {
        MajGrpObjNum = majGrpObjNum;
    }

    public int getMasterGrpObjNum() {
        return MasterGrpObjNum;
    }

    public void setMasterGrpObjNum(int masterGrpObjNum) {
        MasterGrpObjNum = masterGrpObjNum;
    }
}

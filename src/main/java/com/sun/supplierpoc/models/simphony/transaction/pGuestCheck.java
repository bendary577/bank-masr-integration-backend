package com.sun.supplierpoc.models.simphony.transaction;
import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "CheckDateToFire",
        "CheckEmployeeObjectNum",
        "CheckGuestCount",
        "CheckID",
        "CheckNum",
        "CheckOrderType",
        "CheckRevenueCenterID",
        "CheckSeq",
        "CheckStatusBits",
        "CheckTableObjectNum",
        "PCheckInfoLines",
        "EventObjectNum"
})
public class pGuestCheck {
    @XmlElement(name = "CheckDateToFire")
    private String CheckDateToFire;
    @XmlElement(name = "CheckEmployeeObjectNum")
    private String CheckEmployeeObjectNum;
    @XmlElement(name = "CheckGuestCount")
    private String CheckGuestCount;
    @XmlElement(name = "CheckID")
    private String CheckID;
    @XmlElement(name = "CheckNum")
    private String CheckNum;
    @XmlElement(name = "CheckOrderType")
    private String CheckOrderType;
    @XmlElement(name = "CheckRevenueCenterID")
    private String CheckRevenueCenterID;
    @XmlElement(name = "CheckSeq")
    private String CheckSeq;
    @XmlElement(name = "CheckStatusBits")
    private String CheckStatusBits;
    @XmlElement(name = "CheckTableObjectNum")
    private String CheckTableObjectNum;
    @XmlElementWrapper(name="PCheckInfoLines")
    @XmlElement(name="string")
    private List<String> PCheckInfoLines;
    @XmlElement(name = "EventObjectNum")
    private String EventObjectNum;

    public String getCheckDateToFire() {
        return CheckDateToFire;
    }

    public void setCheckDateToFire(String checkDateToFire) {
        CheckDateToFire = checkDateToFire;
    }

    public String getCheckEmployeeObjectNum() {
        return CheckEmployeeObjectNum;
    }

    public void setCheckEmployeeObjectNum(String checkEmployeeObjectNum) {
        CheckEmployeeObjectNum = checkEmployeeObjectNum;
    }

    public String getCheckGuestCount() {
        return CheckGuestCount;
    }

    public void setCheckGuestCount(String checkGuestCount) {
        CheckGuestCount = checkGuestCount;
    }

    public String getCheckID() {
        return CheckID;
    }

    public void setCheckID(String checkID) {
        CheckID = checkID;
    }

    public String getCheckNum() {
        return CheckNum;
    }

    public void setCheckNum(String checkNum) {
        CheckNum = checkNum;
    }

    public String getCheckOrderType() {
        return CheckOrderType;
    }

    public void setCheckOrderType(String checkOrderType) {
        CheckOrderType = checkOrderType;
    }

    public String getCheckRevenueCenterID() {
        return CheckRevenueCenterID;
    }

    public void setCheckRevenueCenterID(String checkRevenueCenterID) {
        CheckRevenueCenterID = checkRevenueCenterID;
    }

    public String getCheckSeq() {
        return CheckSeq;
    }

    public void setCheckSeq(String checkSeq) {
        CheckSeq = checkSeq;
    }

    public String getCheckStatusBits() {
        return CheckStatusBits;
    }

    public void setCheckStatusBits(String checkStatusBits) {
        CheckStatusBits = checkStatusBits;
    }

    public String getCheckTableObjectNum() {
        return CheckTableObjectNum;
    }

    public void setCheckTableObjectNum(String checkTableObjectNum) {
        CheckTableObjectNum = checkTableObjectNum;
    }

    public List<String> getPCheckInfoLines() {
        return PCheckInfoLines;
    }

    public void setPCheckInfoLines(List<String> PCheckInfoLines) {
        this.PCheckInfoLines = PCheckInfoLines;
    }

    public String getEventObjectNum() {
        return EventObjectNum;
    }

    public void setEventObjectNum(String eventObjectNum) {
        EventObjectNum = eventObjectNum;
    }
}

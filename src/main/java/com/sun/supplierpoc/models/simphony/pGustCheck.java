package com.sun.supplierpoc.models.simphony;
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
public class pGustCheck {
    @XmlElement(name = "CheckDateToFire",nillable=true,required = true)
    private String CheckDateToFire;
    @XmlElement(name = "CheckEmployeeObjectNum",nillable=true,required = true)
    private Number CheckEmployeeObjectNum;
    @XmlElement(name = "CheckGuestCount",nillable=true,required = true)
    private Number CheckGuestCount;
    @XmlElement(name = "CheckID",nillable=true,required = true)
    private String CheckID;
    @XmlElement(name = "CheckNum",nillable=true,required = true)
    private Number CheckNum;
    @XmlElement(name = "CheckOrderType",nillable=true,required = true)
    private Number CheckOrderType;
    @XmlElement(name = "CheckRevenueCenterID",nillable=true,required = true)
    private Number CheckRevenueCenterID;
    @XmlElement(name = "CheckSeq",nillable=true,required = true)
    private Number CheckSeq;
    @XmlElement(name = "CheckStatusBits",nillable=true,required = true)
    private Number CheckStatusBits;
    @XmlElement(name = "CheckTableObjectNum",nillable=true,required = true)
    private Number CheckTableObjectNum;
    @XmlElementWrapper(name="PCheckInfoLines")
    @XmlElement(name="string")
    private List<String> PCheckInfoLines;
    @XmlElement(name = "EventObjectNum",nillable=true,required = true)
    private Number EventObjectNum;

    public String getCheckDateToFire() {
        return CheckDateToFire;
    }

    public void setCheckDateToFire(String checkDateToFire) {
        CheckDateToFire = checkDateToFire;
    }

    public Number getCheckEmployeeObjectNum() {
        return CheckEmployeeObjectNum;
    }

    public void setCheckEmployeeObjectNum(Number checkEmployeeObjectNum) {
        CheckEmployeeObjectNum = checkEmployeeObjectNum;
    }

    public Number getCheckGuestCount() {
        return CheckGuestCount;
    }

    public void setCheckGuestCount(Number checkGuestCount) {
        CheckGuestCount = checkGuestCount;
    }

    public String getCheckID() {
        return CheckID;
    }

    public void setCheckID(String checkID) {
        CheckID = checkID;
    }

    public Number getCheckNum() {
        return CheckNum;
    }

    public void setCheckNum(Number checkNum) {
        CheckNum = checkNum;
    }

    public Number getCheckOrderType() {
        return CheckOrderType;
    }

    public void setCheckOrderType(Number checkOrderType) {
        CheckOrderType = checkOrderType;
    }

    public Number getCheckRevenueCenterID() {
        return CheckRevenueCenterID;
    }

    public void setCheckRevenueCenterID(Number checkRevenueCenterID) {
        CheckRevenueCenterID = checkRevenueCenterID;
    }

    public Number getCheckSeq() {
        return CheckSeq;
    }

    public void setCheckSeq(Number checkSeq) {
        CheckSeq = checkSeq;
    }

    public Number getCheckStatusBits() {
        return CheckStatusBits;
    }

    public void setCheckStatusBits(Number checkStatusBits) {
        CheckStatusBits = checkStatusBits;
    }

    public Number getCheckTableObjectNum() {
        return CheckTableObjectNum;
    }

    public void setCheckTableObjectNum(Number checkTableObjectNum) {
        CheckTableObjectNum = checkTableObjectNum;
    }

    public List<String> getPCheckInfoLines() {
        return PCheckInfoLines;
    }

    public void setPCheckInfoLines(List<String> PCheckInfoLines) {
        this.PCheckInfoLines = PCheckInfoLines;
    }

    public Number getEventObjectNum() {
        return EventObjectNum;
    }

    public void setEventObjectNum(Number eventObjectNum) {
        EventObjectNum = eventObjectNum;
    }
}

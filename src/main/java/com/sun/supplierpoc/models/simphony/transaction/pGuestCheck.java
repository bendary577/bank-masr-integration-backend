package com.sun.supplierpoc.models.simphony.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.sun.supplierpoc.models.simphony.PCheckInfoLines;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotBlank(message = "order type can't be blank.")
    @NotNull(message = "order type can't be null.")
    private String CheckOrderType;

    @XmlElement(name = "CheckRevenueCenterID")
    @NotBlank(message = "Revenue center can't be blank.")
    @NotNull(message = "Revenue center can't be null.")
    private String CheckRevenueCenterID;

    @XmlElement(name = "CheckSeq")
    private String CheckSeq;

    @XmlElement(name = "CheckStatusBits")
    private String CheckStatusBits;

    @XmlElement(name = "CheckTableObjectNum")
    private String CheckTableObjectNum;

//    @XmlElementWrapper(name="PCheckInfoLines")
//    @XmlElement(name="string")
//    private List<String> PCheckInfoLines;

    @Valid
    @XmlElement(name = "PCheckInfoLines")
    private PCheckInfoLines PCheckInfoLines;

    @XmlElement(name = "EventObjectNum")
    private String EventObjectNum;

    public String revenue(){
        return CheckRevenueCenterID;
    }

    @JsonIgnore
    @JsonProperty(value = "dateToFire")
    public String getCheckDateToFire() {
        return CheckDateToFire;
    }

    public void setCheckDateToFire(String checkDateToFire) {
        CheckDateToFire = checkDateToFire;
    }

    @JsonIgnore
    @JsonProperty(value = "employeeId")
    public String getCheckEmployeeObjectNum() {
        return CheckEmployeeObjectNum;
    }

    public void setCheckEmployeeObjectNum(String checkEmployeeObjectNum) {
        CheckEmployeeObjectNum = checkEmployeeObjectNum;
    }

    @JsonIgnore
    @JsonProperty(value = "guestCount")
    public String getCheckGuestCount() {
        return CheckGuestCount;
    }

    public void setCheckGuestCount(String checkGuestCount) {
        CheckGuestCount = checkGuestCount;
    }

    @JsonIgnore
    @JsonProperty(value = "checkID")
    public String getCheckID() {
        return CheckID;
    }

    public void setCheckID(String checkID) {
        CheckID = checkID;
    }

    @JsonProperty(value = "checkNumber")
    public String getCheckNum() {
        return CheckNum;
    }

    public void setCheckNum(String checkNum) {
        CheckNum = checkNum;
    }

    @JsonIgnore
    @JsonProperty(value = "orderType")
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

    @JsonIgnore
    @JsonProperty(value = "checkSequence")
    public String getCheckSeq() {
        return CheckSeq;
    }

    public void setCheckSeq(String checkSeq) {
        CheckSeq = checkSeq;
    }

    @JsonIgnore
    @JsonProperty(value = "checkStatus")
    public String getCheckStatusBits() {
        return CheckStatusBits;
    }

    public void setCheckStatusBits(String checkStatusBits) {
        CheckStatusBits = checkStatusBits;
    }

    @JsonIgnore
    @JsonProperty(value = "tableNumber")
    public String getCheckTableObjectNum() {
        return CheckTableObjectNum;
    }

    public void setCheckTableObjectNum(String checkTableObjectNum) {
        CheckTableObjectNum = checkTableObjectNum;
    }

    @JsonIgnore
    @JsonProperty(value = "checkInfoLines")
    public com.sun.supplierpoc.models.simphony.PCheckInfoLines getPCheckInfoLines() {
        return PCheckInfoLines;
    }

    @JsonProperty(value = "checkInfoLines")
    public void setPCheckInfoLines(com.sun.supplierpoc.models.simphony.PCheckInfoLines PCheckInfoLines) {
        this.PCheckInfoLines = PCheckInfoLines;
    }

    @JsonIgnore
    @JsonProperty(value = "eventObjectNum")
    public String getEventObjectNum() {
        return EventObjectNum;
    }

    public void setEventObjectNum(String eventObjectNum) {
        EventObjectNum = eventObjectNum;
    }
}
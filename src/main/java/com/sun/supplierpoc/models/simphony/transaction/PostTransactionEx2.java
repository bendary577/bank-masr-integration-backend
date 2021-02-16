package com.sun.supplierpoc.models.simphony.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.supplierpoc.models.simphony.ppMenuItemsEx;
import com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "pGuestCheck",
        "ppMenuItemsEx",
        "pTmedDetailEx2"
})

@XmlRootElement(name = "PostTransactionEx2")
public class PostTransactionEx2 {
    private pGuestCheck pGuestCheck;

    @JsonProperty(value = "menuItems")
    private ppMenuItemsEx ppMenuItemsEx;

    private pTmedDetailEx2 pTmedDetailEx2;

    public pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public ppMenuItemsEx getPpMenuItemsEx() {
        return ppMenuItemsEx;
    }

    public void setPpMenuItemsEx(ppMenuItemsEx ppMenuItemsEx) {
        this.ppMenuItemsEx = ppMenuItemsEx;
    }

    public com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 getpTmedDetailEx2() {
        return pTmedDetailEx2;
    }

    public void setpTmedDetailEx2(com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 pTmedDetailEx2) {
        this.pTmedDetailEx2 = pTmedDetailEx2;
    }
}

package com.sun.supplierpoc.models.simphony.transaction;

import com.sun.supplierpoc.models.simphony.ppMenuItemsEx;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PostTransactionEx2Response")
public class PostTransactionEx2Response {
    @XmlElement(name = "pGuestCheck")
    private pGuestCheck pGuestCheck;
    @XmlElement(name = "ppMenuItemsEx")
    private ppMenuItemsEx ppMenuItemsEx;

    public pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public ppMenuItemsEx getPpMenuItemsEx() {
        return ppMenuItemsEx;
    }

    public void setPpMenuItemsEx(com.sun.supplierpoc.models.simphony.ppMenuItemsEx ppMenuItemsEx) {
        this.ppMenuItemsEx = ppMenuItemsEx;
    }
}

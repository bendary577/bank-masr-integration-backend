package com.sun.supplierpoc.models.simphony.transaction;

import com.sun.supplierpoc.models.simphony.ppMenuItemsEx;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PostTransactionEx2")
public class PostTransactionEx2 {
    private pGuestCheck pGuestCheck;

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

    public void setPpMenuItemsEx(ppMenuItemsEx ppMenuItemsEx) {
        this.ppMenuItemsEx = ppMenuItemsEx;
    }
}

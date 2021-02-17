package com.sun.supplierpoc.models.simphony.transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.supplierpoc.models.simphony.SimphonyMenuItem;
import com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "pGuestCheck",
        "ppMenuItemsEx",
        "pTmedDetailEx2"
})

@XmlRootElement(name = "PostTransactionEx2")
public class PostTransactionEx2 {
    @JsonProperty(value = "guestCheck")
    private pGuestCheck pGuestCheck;

    @JsonProperty(value = "menuItems")
    @XmlElementWrapper(name = "ppMenuItemsEx")
    @XmlElement(name = "SimphonyPosApi_MenuItemEx")
    private List<SimphonyMenuItem> ppMenuItemsEx;

    private pTmedDetailEx2 pTmedDetailEx2;

    public pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public List<SimphonyMenuItem> getPpMenuItemsEx() {
        return ppMenuItemsEx;
    }

    public void setPpMenuItemsEx(List<SimphonyMenuItem> ppMenuItemsEx) {
        this.ppMenuItemsEx = ppMenuItemsEx;
    }

    public pTmedDetailEx2 getpTmedDetailEx2() {
        return pTmedDetailEx2;
    }

    public void setpTmedDetailEx2(pTmedDetailEx2 pTmedDetailEx2) {
        this.pTmedDetailEx2 = pTmedDetailEx2;
    }
}

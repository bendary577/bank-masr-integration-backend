package com.sun.supplierpoc.models.simphony.transaction;
import com.sun.supplierpoc.models.simphony.ppMenuItems;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "vendorCode",
        "pGuestCheck",
        "ppMenuItems",
        "ppComboMeals"
})
@XmlRootElement(name = "PostTransactionEx")

public class PostTransactionEx {
    @XmlElement(name = "vendorCode",nillable=true,required = true)
    protected String vendorCode;
    @XmlElement(name = "pGuestCheck",nillable=true,required = true)
    protected com.sun.supplierpoc.models.simphony.transaction.pGuestCheck pGuestCheck;
    @XmlElement(name = "ppMenuItems",nillable=true,required = true)
    protected com.sun.supplierpoc.models.simphony.ppMenuItems ppMenuItems;
    @XmlElement(name = "ppComboMeals",nillable=true,required = true)
    protected com.sun.supplierpoc.models.simphony.ppComboMeals ppComboMeals;

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public com.sun.supplierpoc.models.simphony.transaction.pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(com.sun.supplierpoc.models.simphony.transaction.pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public ppMenuItems getPpMenuItems() {
        return ppMenuItems;
    }

    public void setPpMenuItems(ppMenuItems ppMenuItems) {
        this.ppMenuItems = ppMenuItems;
    }
}

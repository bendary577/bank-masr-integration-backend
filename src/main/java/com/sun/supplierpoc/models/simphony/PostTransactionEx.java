package com.sun.supplierpoc.models.simphony;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "vendorCode",
        "pGuestCheck",
        "ppMenuItems",
        "ppComboMeals"/*,
        "pSvcCharge",
        "pSubTotalDiscount",
        "pTmedDetail",
        "pTotalsResponse",
        "ppCheckPrintLines",
        "ppVoucherOutput"*/
})
@XmlRootElement(name = "PostTransactionEx")

public class PostTransactionEx {
    @XmlElement(name = "vendorCode",nillable=true,required = true)
    protected String vendorCode;
    @XmlElement(name = "pGuestCheck",nillable=true,required = true)
    protected pGustCheck pGuestCheck;
    @XmlElement(name = "ppMenuItems",nillable=true,required = true)
    protected ppMenuItems ppMenuItems;
    @XmlElement(name = "ppComboMeals",nillable=true,required = true)
    protected ppComboMeals ppComboMeals;
    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public pGustCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGustCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public ppMenuItems getPpMenuItems() {
        return ppMenuItems;
    }

    public void setPpMenuItems(ppMenuItems ppMenuItems) {
        this.ppMenuItems = ppMenuItems;
    }
}

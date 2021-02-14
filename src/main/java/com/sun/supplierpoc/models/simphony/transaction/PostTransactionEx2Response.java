package com.sun.supplierpoc.models.simphony.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.supplierpoc.models.simphony.serviceCharge.pSvcChargeEx;
import com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "pSvcChargeEx",
        "pGuestCheck",
        "pTmedDetailEx2"
})

public class PostTransactionEx2Response {
    @JsonIgnore
    @XmlElement(name = "pSvcChargeEx")
    private pSvcChargeEx pSvcChargeEx;

    @XmlElement(name = "pGuestCheck")
    private pGuestCheck pGuestCheck;

    @JsonIgnore
    @XmlElement(name = "pTmedDetailEx2")
    private pTmedDetailEx2 pTmedDetailEx2;

    @XmlElement(name = "pTotalsResponseEx")
    private pTotalsResponseEx pTotalsResponseEx;

    public com.sun.supplierpoc.models.simphony.serviceCharge.pSvcChargeEx getpSvcChargeEx() {
        return pSvcChargeEx;
    }

    public void setpSvcChargeEx(com.sun.supplierpoc.models.simphony.serviceCharge.pSvcChargeEx pSvcChargeEx) {
        this.pSvcChargeEx = pSvcChargeEx;
    }

    @JsonProperty(value = "guestCheck")
    public pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 getpTmedDetailEx2() {
        return pTmedDetailEx2;
    }

    public void setpTmedDetailEx2(com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 pTmedDetailEx2) {
        this.pTmedDetailEx2 = pTmedDetailEx2;
    }

    @JsonProperty(value = "totalsResponse")
    public com.sun.supplierpoc.models.simphony.transaction.pTotalsResponseEx getpTotalsResponseEx() {
        return pTotalsResponseEx;
    }

    public void setpTotalsResponseEx(com.sun.supplierpoc.models.simphony.transaction.pTotalsResponseEx pTotalsResponseEx) {
        this.pTotalsResponseEx = pTotalsResponseEx;
    }
}

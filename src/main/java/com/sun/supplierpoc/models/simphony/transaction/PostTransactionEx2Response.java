package com.sun.supplierpoc.models.simphony.transaction;

import com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)

public class PostTransactionEx2Response {
//    @XmlElement(name = "pSvcChargeEx")
//    private pSvcChargeEx pSvcChargeEx;
    @XmlElement(name = "pGuestCheck")
    private pGuestCheck pGuestCheck;
    @XmlElement(name = "pTmedDetailEx2")
    private pTmedDetailEx2 tenderDetail;
    @XmlElement(name = "pTotalsResponseEx")
    private pTotalsResponseEx checkTotals;

    public pGuestCheck getpGuestCheck() {
        return pGuestCheck;
    }

    public void setpGuestCheck(pGuestCheck pGuestCheck) {
        this.pGuestCheck = pGuestCheck;
    }

    public com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 getTenderDetail() {
        return tenderDetail;
    }

    public void setTenderDetail(com.sun.supplierpoc.models.simphony.tender.pTmedDetailEx2 tenderDetail) {
        this.tenderDetail = tenderDetail;
    }

    public com.sun.supplierpoc.models.simphony.transaction.pTotalsResponseEx getCheckTotals() {
        return checkTotals;
    }

    public void setCheckTotals(com.sun.supplierpoc.models.simphony.transaction.pTotalsResponseEx checkTotals) {
        this.checkTotals = checkTotals;
    }
}

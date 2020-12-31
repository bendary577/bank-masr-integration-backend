package com.sun.supplierpoc.models.simphony.transaction;

import com.sun.supplierpoc.models.simphony.OperationalResult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "TotalsOtherTotals",
        "TotalsAutoSvcChgTotals",
        "OperationalResult",
        "TotalsTaxTotals",
        "TotalsTotalDue",
        "TotalsSubTotal",
})

public class pTotalsResponseEx {
    @XmlElement(name = "TotalsOtherTotals")
    private String TotalsOtherTotals;
    @XmlElement(name = "TotalsAutoSvcChgTotals")
    private String TotalsAutoSvcChgTotals;

    @XmlElement(name = "OperationalResult")
    private OperationalResult OperationalResult;

    @XmlElement(name = "TotalsTaxTotals")
    private String TotalsTaxTotals;
    @XmlElement(name = "TotalsTotalDue")
    private String TotalsTotalDue;
    @XmlElement(name = "TotalsSubTotal")
    private String TotalsSubTotal;

    public String getTotalsOtherTotals() {
        return TotalsOtherTotals;
    }

    public void setTotalsOtherTotals(String totalsOtherTotals) {
        TotalsOtherTotals = totalsOtherTotals;
    }

    public String getTotalsAutoSvcChgTotals() {
        return TotalsAutoSvcChgTotals;
    }

    public void setTotalsAutoSvcChgTotals(String totalsAutoSvcChgTotals) {
        TotalsAutoSvcChgTotals = totalsAutoSvcChgTotals;
    }

    public com.sun.supplierpoc.models.simphony.OperationalResult getOperationalResult() {
        return OperationalResult;
    }

    public void setOperationalResult(com.sun.supplierpoc.models.simphony.OperationalResult operationalResult) {
        OperationalResult = operationalResult;
    }

    public String getTotalsTaxTotals() {
        return TotalsTaxTotals;
    }

    public void setTotalsTaxTotals(String totalsTaxTotals) {
        TotalsTaxTotals = totalsTaxTotals;
    }

    public String getTotalsTotalDue() {
        return TotalsTotalDue;
    }

    public void setTotalsTotalDue(String totalsTotalDue) {
        TotalsTotalDue = totalsTotalDue;
    }

    public String getTotalsSubTotal() {
        return TotalsSubTotal;
    }

    public void setTotalsSubTotal(String totalsSubTotal) {
        TotalsSubTotal = totalsSubTotal;
    }
}

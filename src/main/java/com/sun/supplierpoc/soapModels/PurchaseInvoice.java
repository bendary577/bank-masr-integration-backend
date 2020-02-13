package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="PurchaseInvoice")
public class PurchaseInvoice {
    String status;

    @XmlElement(name="PurchaseInvoiceReference")
    String PurchaseInvoiceReference;
    @XmlElement(name="PurchaseTransactionType")
    String PurchaseTransactionType;

    @XmlAttribute(name="status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPurchaseInvoiceReference() {
        return PurchaseInvoiceReference;
    }

    public void setPurchaseInvoiceReference(String purchaseInvoiceReference) {
        PurchaseInvoiceReference = purchaseInvoiceReference;
    }

    public String getPurchaseTransactionType() {
        return PurchaseTransactionType;
    }

    public void setPurchaseTransactionType(String purchaseTransactionType) {
        PurchaseTransactionType = purchaseTransactionType;
    }
}

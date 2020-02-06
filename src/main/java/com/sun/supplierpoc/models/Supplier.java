package com.sun.supplierpoc.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Supplier")
public class Supplier {
    @XmlElement
    String AccountCode;
    @XmlElement
    String SupplierName;
    @XmlElement
    String SupplierCode;
    @XmlElement
    String Status;
    @XmlElement
    String PaymentTermsGroupCode;
    @XmlElement
    SupplierAddress SupplierAddress;
    @XmlElement
    Address_Contact Address_Contact;

    public String getAccountCode() {
        return AccountCode;
    }

    public void setAccountCode(String accountCode) {
        AccountCode = accountCode;
    }

    public String getSupplierName() {
        return SupplierName;
    }

    public void setSupplierName(String supplierName) {
        SupplierName = supplierName;
    }

    public String getSupplierCode() {
        return SupplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        SupplierCode = supplierCode;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getPaymentTermsGroupCode() {
        return PaymentTermsGroupCode;
    }

    public void setPaymentTermsGroupCode(String paymentTermsGroupCode) {
        PaymentTermsGroupCode = paymentTermsGroupCode;
    }

    public com.sun.supplierpoc.models.SupplierAddress getSupplierAddress() {
        return SupplierAddress;
    }

    public void setSupplierAddress(com.sun.supplierpoc.models.SupplierAddress supplierAddress) {
        SupplierAddress = supplierAddress;
    }

    public com.sun.supplierpoc.models.Address_Contact getAddress_Contact() {
        return Address_Contact;
    }

    public void setAddress_Contact(com.sun.supplierpoc.models.Address_Contact address_Contact) {
        Address_Contact = address_Contact;
    }
}

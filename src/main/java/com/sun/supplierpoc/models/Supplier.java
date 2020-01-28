package com.sun.supplierpoc.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Supplier")
public class Supplier {
    @XmlElement
    String AccountCode;
    @XmlElement
    String SupplierName;

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
}

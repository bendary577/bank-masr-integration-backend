package com.sun.supplierpoc.models;

import javax.xml.bind.annotation.XmlElement;

public class SupplierAddress {
    @XmlElement
    String TelephoneNumber;
    @XmlElement
    String AddressCode;
    @XmlElement
    String AddressLine1;
    @XmlElement
    String AddressLine2;
    @XmlElement
    String AddressLine3;
    @XmlElement
    String PostalCode;

    public String getTelephoneNumber() {
        return TelephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        TelephoneNumber = telephoneNumber;
    }

    public String getAddressLine1() {
        return AddressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        AddressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return AddressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        AddressLine2 = addressLine2;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getAddressCode() {
        return AddressCode;
    }

    public void setAddressCode(String addressCode) {
        AddressCode = addressCode;
    }

    public String getAddressLine3() {
        return AddressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        AddressLine3 = addressLine3;
    }
}

package com.sun.supplierpoc.soapModels;

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

    public SupplierAddress() {
    }

    public String getTelephoneNumber() {
        if(TelephoneNumber != null){
            return TelephoneNumber;
        }
        else return "";
    }

    public void setTelephoneNumber(String telephoneNumber) {
        TelephoneNumber = telephoneNumber;
    }

    public String getAddressLine1() {
        if(AddressLine1 != null){
            return AddressLine1;
        }
        else return "";

    }

    public void setAddressLine1(String addressLine1) {
        AddressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        if(AddressLine2 != null){
            return AddressLine2;
        }
        else return "";
    }

    public void setAddressLine2(String addressLine2) {
        AddressLine2 = addressLine2;
    }


    public String getAddressLine3() {
        if(AddressLine3 != null){
            return AddressLine3;
        }
        else return "";
    }

    public void setAddressLine3(String addressLine3) {
        AddressLine3 = addressLine3;
    }

    public String getPostalCode() {
        if(PostalCode != null){
            return PostalCode;
        }
        else return "";
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getAddressCode() {
        if(AddressCode != null){
            return AddressCode;
        }
        else return "";
    }

    public void setAddressCode(String addressCode) {
        AddressCode = addressCode;
    }

}

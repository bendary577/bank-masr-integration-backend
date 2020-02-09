package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;

public class Address_Contact {
    @XmlElement
    String ContactIdentifier;

    public String getContactIdentifier() {
        if(ContactIdentifier != null){
            return ContactIdentifier;
        }
        else return "";
    }

    public void setContactIdentifier(String contactIdentifier) {
        ContactIdentifier = contactIdentifier;
    }
}

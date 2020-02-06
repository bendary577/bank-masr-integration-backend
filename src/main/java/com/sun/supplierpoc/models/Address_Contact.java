package com.sun.supplierpoc.models;

import javax.xml.bind.annotation.XmlElement;

public class Address_Contact {
    @XmlElement
    String ContactIdentifier;

    public String getContactIdentifier() {
        return ContactIdentifier;
    }

    public void setContactIdentifier(String contactIdentifier) {
        ContactIdentifier = contactIdentifier;
    }
}

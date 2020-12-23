package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;

public class Accounts {
    @XmlElement
    String Description;

    public Accounts() {
    }

    public Accounts(String description) {
        Description = description;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}

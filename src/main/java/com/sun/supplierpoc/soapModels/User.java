package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;

public class User {
    @XmlElement
    String Name;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}

package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

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

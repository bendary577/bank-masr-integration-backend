package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class MenuItemName {
    @XmlElement(name = "StringText")
    private String StringText ;

    @XmlElement(name = "StringNumberId")
    private String StringNumberId ;

    public String getStringText() {
        return StringText;
    }

    public void setStringText(String stringText) {
        StringText = stringText;
    }

    public String getStringNumberId() {
        return StringNumberId;
    }

    public void setStringNumberId(String stringNumberId) {
        StringNumberId = stringNumberId;
    }
}

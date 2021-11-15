package com.sun.supplierpoc.models.simphony.masters;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Name")
public class Name {

    @XmlElement(name = "StringNumberId")
    public int StringNumberId;
    @XmlElement(name = "StringText")
    public String StringText;


    public int getStringNumberId() {
        return StringNumberId;
    }

    public void setStringNumberId(int stringNumberId) {
        StringNumberId = stringNumberId;
    }

    public String getStringText() {
        return StringText;
    }

    public void setStringText(String stringText) {
        StringText = stringText;
    }
}
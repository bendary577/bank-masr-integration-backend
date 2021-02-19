package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.XmlElement;

public class RefillDescriptor {
    @XmlElement(name = "CondimentOrderingType")
    public int StringNumberId;
    @XmlElement(name = "CondimentOrderingType")
    public Object StringText;

    public int getStringNumberId() {
        return StringNumberId;
    }

    public void setStringNumberId(int stringNumberId) {
        StringNumberId = stringNumberId;
    }

    public Object getStringText() {
        return StringText;
    }

    public void setStringText(Object stringText) {
        StringText = stringText;
    }
}
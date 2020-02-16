package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Ledger")

public class Ledger {
    @XmlElement(name="Line")
    Line Line;

    public com.sun.supplierpoc.soapModels.Line getLine() {
        return Line;
    }

    public void setLine(com.sun.supplierpoc.soapModels.Line line) {
        Line = line;
    }
}

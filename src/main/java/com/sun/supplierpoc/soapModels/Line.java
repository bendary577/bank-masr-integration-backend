package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement(name="Line")

public class Line {
    String status;

    @XmlElement(name="Messages")
    Messages Messages;

    @XmlAttribute(name="status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Messages getMessages() {
        return Messages;
    }

    public void setMessages(Messages messages) {
        Messages = messages;
    }
}

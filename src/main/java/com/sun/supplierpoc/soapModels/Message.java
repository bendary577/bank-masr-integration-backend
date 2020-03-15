package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Message")
public class Message {
    private String Level;

    @XmlElement(name="UserText")
    private String UserText;


    @XmlAttribute(name="Level")
    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }

    public String getUserText() {
        return UserText;
    }

    public void setUserText(String userText) {
        UserText = userText;
    }
}

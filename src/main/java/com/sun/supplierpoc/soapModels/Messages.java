package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlRootElement(name="Messages")
public class Messages {
    @XmlElement(name="Message")
    ArrayList<Message> Message;

    public ArrayList<Message> getMessage() {
        return Message;
    }

    public void setMessage(ArrayList<Message> message) {
        Message = message;
    }
}

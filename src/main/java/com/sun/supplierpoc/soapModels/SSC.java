package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;

@XmlRootElement(name="SSC")
@XmlAccessorType(XmlAccessType.FIELD)
public class SSC implements Serializable {
    com.sun.supplierpoc.soapModels.User User;
    Object SunSystemsContext;
    @XmlElementWrapper(name="Payload")
    @XmlElement(name="Supplier")
    ArrayList<Supplier> Payload;

    public com.sun.supplierpoc.soapModels.User getUser() {
        return User;
    }

    public void setUser(com.sun.supplierpoc.soapModels.User user) {
        User = user;
    }

    public Object getSunSystemsContext() {
        return SunSystemsContext;
    }

    public void setSunSystemsContext(Object sunSystemsContext) {
        SunSystemsContext = sunSystemsContext;
    }

    public ArrayList<Supplier> getPayload() {
        return Payload;
    }

    public void setPayload(ArrayList<Supplier> payload) {
        Payload = payload;
    }
}
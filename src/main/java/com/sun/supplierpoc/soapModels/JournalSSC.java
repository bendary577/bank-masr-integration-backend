package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;

@XmlRootElement(name="SSC")
@XmlAccessorType(XmlAccessType.FIELD)
public class JournalSSC implements Serializable {
    private User User;
    private Object SunSystemsContext;

    @XmlElementWrapper(name="Payload")
    @XmlElement(name="Ledger")
    private ArrayList<Ledger> Payload;


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

    public ArrayList<Ledger> getPayload() {
        return Payload;
    }

    public void setPayload(ArrayList<Ledger> payload) {
        Payload = payload;
    }
}

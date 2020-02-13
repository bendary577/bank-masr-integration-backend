package com.sun.supplierpoc.soapModels;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;

@XmlRootElement(name="SSC")
@XmlAccessorType(XmlAccessType.FIELD)
public class PurchaseInvoiceSSC implements Serializable{
    private User User;
    private Object SunSystemsContext;

    @XmlElementWrapper(name="Payload")
    @XmlElement(name="PurchaseInvoice")
    private ArrayList<PurchaseInvoice> Payload;

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

    public ArrayList<PurchaseInvoice> getPayload() {
        return Payload;
    }

    public void setPayload(ArrayList<PurchaseInvoice> payload) {
        Payload = payload;
    }
}

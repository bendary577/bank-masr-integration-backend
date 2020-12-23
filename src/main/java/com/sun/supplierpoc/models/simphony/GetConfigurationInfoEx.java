package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "configInfoRequest"
})
@XmlRootElement(name = "GetConfigurationInfoEx")

public class GetConfigurationInfoEx {
    @XmlElement(name="configInfoRequest",nillable=true,required = true)
    private configInfoRequest configInfoRequest;

    public configInfoRequest getConfigInfoRequest() {
        return configInfoRequest;
    }

    public void setConfigInfoRequest(configInfoRequest configInfoRequest) {
        this.configInfoRequest = configInfoRequest;
    }
}

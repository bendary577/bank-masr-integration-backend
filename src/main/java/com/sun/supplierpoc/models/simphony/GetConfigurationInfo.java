package com.sun.supplierpoc.models.simphony;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "vendorCode",
        "employeeObjectNum",
        "configurationInfoType",
        "revenueCenter",
        "configInfoResponse",
})
@XmlRootElement(name = "GetConfigurationInfo")
public class GetConfigurationInfo {
    @XmlElement(name = "vendorCode",nillable=true,required = true)
    protected String vendorCode;
    @XmlElement(name = "employeeObjectNum",nillable=true,required = true,type = int.class)
    protected Number employeeObjectNum;
    @XmlElementWrapper(name="configurationInfoType",nillable=true,required = true)
    @XmlElement(name="int",type = int.class)
    private List<Number> configurationInfoType;
    @XmlElement(name = "revenueCenter",nillable=true,required = true,type = int.class)
    private Number revenueCenter;
    @XmlElement(name = "configInfoResponse",nillable=true,required = true)
    private String configInfoResponse;

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public Number getEmployeeObjectNum() {
        return employeeObjectNum;
    }

    public void setEmployeeObjectNum(Number employeeObjectNum) {
        this.employeeObjectNum = employeeObjectNum;
    }

    public List<Number> getConfigurationInfoType() {
        return configurationInfoType;
    }

    public void setConfigurationInfoType(List<Number> configurationInfoType) {
        this.configurationInfoType = configurationInfoType;
    }

    public Number getRevenueCenter() {
        return revenueCenter;
    }

    public void setRevenueCenter(Number revenueCenter) {
        this.revenueCenter = revenueCenter;
    }

    public String getConfigInfoResponse() {
        return configInfoResponse;
    }

    public void setConfigInfoResponse(String configInfoResponse) {
        this.configInfoResponse = configInfoResponse;
    }
}

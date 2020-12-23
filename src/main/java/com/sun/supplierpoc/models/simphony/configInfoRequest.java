package com.sun.supplierpoc.models.simphony;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "vendorCode",
        "EmployeeObjectNumber",
        "RVCObjectNumber",
        "ConfigurationInfo"
})
@XmlRootElement(name = "configInfoRequest")

public class configInfoRequest {
    @XmlElement(name = "vendorCode",nillable=true,required = true)
    protected String vendorCode;

    @XmlElement(name = "EmployeeObjectNumber",nillable=true,required = true,type = int.class)
    protected Number EmployeeObjectNumber;

    @XmlElement(name = "RVCObjectNumber",nillable=true,required = true,type = int.class)
    private Number RVCObjectNumber;

    @XmlElementWrapper(name="ConfigurationInfo",nillable=true,required = true)
    @XmlElement(name="SimphonyPosApi_ConfigInfo")

    private ArrayList<SimphonyPosApi_ConfigInfo> ConfigurationInfo;

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public Number getEmployeeObjectNumber() {
        return EmployeeObjectNumber;
    }

    public void setEmployeeObjectNumber(Number employeeObjectNumber) {
        EmployeeObjectNumber = employeeObjectNumber;
    }

    public Number getRVCObjectNumber() {
        return RVCObjectNumber;
    }

    public void setRVCObjectNumber(Number RVCObjectNumber) {
        this.RVCObjectNumber = RVCObjectNumber;
    }

    public ArrayList<SimphonyPosApi_ConfigInfo> getConfigurationInfo() {
        return ConfigurationInfo;
    }

    public void setConfigurationInfo(ArrayList<SimphonyPosApi_ConfigInfo> configurationInfo) {
        ConfigurationInfo = configurationInfo;
    }
}

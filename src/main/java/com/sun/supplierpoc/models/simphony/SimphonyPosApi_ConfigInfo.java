package com.sun.supplierpoc.models.simphony;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SimphonyPosApi_ConfigInfo")
public class SimphonyPosApi_ConfigInfo {
    @XmlElement(name = "ConfigurationInfoTypeID")
    private String ConfigurationInfoTypeID;
    private int StartIndex;
    private int MaxRecordCount;


    public String getConfigurationInfoTypeID() {
        return ConfigurationInfoTypeID;
    }

    public void setConfigurationInfoTypeID(String ConfigurationInfoTypeID) {
        this.ConfigurationInfoTypeID = ConfigurationInfoTypeID;
    }

    public int getStartIndex() {
        return StartIndex;
    }

    public void setStartIndex(int startIndex) {
        StartIndex = startIndex;
    }

    public int getMaxRecordCount() {
        return MaxRecordCount;
    }

    public void setMaxRecordCount(int maxRecordCount) {
        MaxRecordCount = maxRecordCount;
    }
}

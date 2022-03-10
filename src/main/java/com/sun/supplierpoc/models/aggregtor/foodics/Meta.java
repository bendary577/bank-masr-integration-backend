package com.sun.supplierpoc.models.aggregtor.foodics;


import java.util.HashMap;
import java.util.Map;

public class Meta {

    private String _3rdPartyOrderNumber;

    private String externalNumber;

    private String externalAdditionalProductInfo;

    private String externalAdditionalPaymentInfo;

    public String getExternalAdditionalProductInfo() {
        return externalAdditionalProductInfo;
    }

    public void setExternalAdditionalProductInfo(String externalAdditionalProductInfo) {
        this.externalAdditionalProductInfo = externalAdditionalProductInfo;
    }

    public String get3rdPartyOrderNumber() {
        return _3rdPartyOrderNumber;
    }

    public void set3rdPartyOrderNumber(String _3rdPartyOrderNumber) {
        this._3rdPartyOrderNumber = _3rdPartyOrderNumber;
    }

    public String getExternalAdditionalPaymentInfo() {
        return externalAdditionalPaymentInfo;
    }

    public void setExternalAdditionalPaymentInfo(String externalAdditionalPaymentInfo) {
        this.externalAdditionalPaymentInfo = externalAdditionalPaymentInfo;
    }

    public String getExternalNumber() {
        return externalNumber;
    }

    public void setExternalNumber(String externalNumber) {
        this.externalNumber = externalNumber;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("3rdPartyOrderNumber", _3rdPartyOrderNumber);
        map.put("externalNumber", externalNumber);
        map.put("externalAdditionalProductInfo", externalAdditionalProductInfo);
        map.put("externalAdditionalPaymentInfo", externalAdditionalPaymentInfo);
        return map;
    }
}

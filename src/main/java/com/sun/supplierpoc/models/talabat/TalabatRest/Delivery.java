package com.sun.supplierpoc.models.talabat.TalabatRest;

public class Delivery {

    private String provider;
    private String fee;
    private String postCode;
    private String city;
    private String addressText;
    private String packagingCharges;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public String getPackagingCharges() {
        return packagingCharges;
    }

    public void setPackagingCharges(String packagingCharges) {
        this.packagingCharges = packagingCharges;
    }
}
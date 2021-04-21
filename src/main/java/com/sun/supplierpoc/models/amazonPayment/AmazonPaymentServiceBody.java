package com.sun.supplierpoc.models.amazonPayment;

public class AmazonPaymentServiceBody {
    private String service_command;
    private String access_code;
    private String merchant_identifier;
    private String merchant_reference;
    private String language;
    private String expiry_date;
    private String card_number;
    private String card_security_code;
    private String signature;
    private String token_name;
    private String card_holder_name;
    private String remember_me;
    private String return_url;


    // Getter Methods

    public String getService_command() {
        return service_command;
    }

    public String getAccess_code() {
        return access_code;
    }

    public String getMerchant_identifier() {
        return merchant_identifier;
    }

    public String getMerchant_reference() {
        return merchant_reference;
    }

    public String getLanguage() {
        return language;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public String getCard_number() {
        return card_number;
    }

    public String getCard_security_code() {
        return card_security_code;
    }

    public String getSignature() {
        return signature;
    }

    public String getToken_name() {
        return token_name;
    }

    public String getCard_holder_name() {
        return card_holder_name;
    }

    public String getRemember_me() {
        return remember_me;
    }

    public String getReturn_url() {
        return return_url;
    }

    // Setter Methods

    public void setService_command(String service_command) {
        this.service_command = service_command;
    }

    public void setAccess_code(String access_code) {
        this.access_code = access_code;
    }

    public void setMerchant_identifier(String merchant_identifier) {
        this.merchant_identifier = merchant_identifier;
    }

    public void setMerchant_reference(String merchant_reference) {
        this.merchant_reference = merchant_reference;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setExpiry_date(String expiry_date) {
        this.expiry_date = expiry_date;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public void setCard_security_code(String card_security_code) {
        this.card_security_code = card_security_code;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setToken_name(String token_name) {
        this.token_name = token_name;
    }

    public void setCard_holder_name(String card_holder_name) {
        this.card_holder_name = card_holder_name;
    }

    public void setRemember_me(String remember_me) {
        this.remember_me = remember_me;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }
}

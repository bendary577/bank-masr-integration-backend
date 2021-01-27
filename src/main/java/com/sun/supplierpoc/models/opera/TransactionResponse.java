package com.sun.supplierpoc.models.opera;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;

@JacksonXmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse implements Serializable {
    @JacksonXmlProperty(localName = "SequenceNo")
    private String sequenceNo;
    @JacksonXmlProperty(localName = "TransType")
    private String transType;
    @JacksonXmlProperty(localName = "TransAmount")
    private String transAmount;
    @JacksonXmlProperty(localName = "OtherAmount")
    private String otherAmount;
    @JacksonXmlProperty(localName = "RespCode")
    private String respCode;
    @JacksonXmlProperty(localName = "RespText")
    private String respText;
    @JacksonXmlProperty(localName = "PAN")
    private String pAN;
    @JacksonXmlProperty(localName = "ExpiryDate")
    private String expiryDate;
    @JacksonXmlProperty(localName = "TransToken")
    private String transToken;
    @JacksonXmlProperty(localName = "EntryMode")
    private String entryMode;
    @JacksonXmlProperty(localName = "IssuerId")
    private String issuerId;
    @JacksonXmlProperty(localName = "RRN")
    private String rRN;
    @JacksonXmlProperty(localName = "OfflineFlag")
    private String offlineFlag;
    @JacksonXmlProperty(localName = "MerchantId")
    private String merchantId;
    @JacksonXmlProperty(localName = "TerminalId")
    private String terminalId;
    @JacksonXmlProperty(localName = "Balance")
    private String balance;
    @JacksonXmlProperty(localName = "AuthCode")
    private String authCode;
    @JacksonXmlProperty(localName = "DCCIndicator")
    private String dCCIndicator;
    @JacksonXmlProperty(localName = "BillingCurrency")
    private String billingCurrency;
    @JacksonXmlProperty(localName = "BillingAmount")
    private String billingAmount;
    @JacksonXmlProperty(localName = "DCCExchangeRate")
    private String dCCExchangeRate;
    @JacksonXmlProperty(localName = "printData")
    private String printData;


    public TransactionResponse() {
    }


    public String getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(String sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespText() {
        return respText;
    }

    public void setRespText(String respText) {
        this.respText = respText;
    }

    public String getpAN() {
        return pAN;
    }

    public void setpAN(String pAN) {
        this.pAN = pAN;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTransToken() {
        return transToken;
    }

    public void setTransToken(String transToken) {
        this.transToken = transToken;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public String getrRN() {
        return rRN;
    }

    public void setrRN(String rRN) {
        this.rRN = rRN;
    }

    public String getOfflineFlag() {
        return offlineFlag;
    }

    public void setOfflineFlag(String offlineFlag) {
        this.offlineFlag = offlineFlag;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(String transAmount) {
        this.transAmount = transAmount;
    }

    public String getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(String otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getdCCIndicator() {
        return dCCIndicator;
    }

    public void setdCCIndicator(String dCCIndicator) {
        this.dCCIndicator = dCCIndicator;
    }

    public String getBillingCurrency() {
        return billingCurrency;
    }

    public void setBillingCurrency(String billingCurrency) {
        this.billingCurrency = billingCurrency;
    }

    public String getBillingAmount() {
        return billingAmount;
    }

    public void setBillingAmount(String billingAmount) {
        this.billingAmount = billingAmount;
    }

    public String getdCCExchangeRate() {
        return dCCExchangeRate;
    }

    public void setdCCExchangeRate(String dCCExchangeRate) {
        this.dCCExchangeRate = dCCExchangeRate;
    }

    public String getPrintData() {
        return printData;
    }

    public void setPrintData(String printData) {
        this.printData = printData;
    }
}

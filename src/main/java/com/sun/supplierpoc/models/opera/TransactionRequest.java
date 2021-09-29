package com.sun.supplierpoc.models.opera;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@JacksonXmlRootElement
public class TransactionRequest implements Serializable {
    @JacksonXmlProperty
    private String SequenceNo;
    @JacksonXmlProperty
    private String TransType;
    @JacksonXmlProperty
    private String TransAmount;
    @JacksonXmlProperty
    private String TransCurrency;
    @JacksonXmlProperty
    private String TransToken;
    @JacksonXmlProperty
    private String IssuerId;
    @JacksonXmlProperty
    private String PAN;
    @JacksonXmlProperty
    private String ExpiryDate;
    @JacksonXmlProperty
    private String TaxAmount;
    @JacksonXmlProperty
    private String TransDateTime;
    @JacksonXmlProperty
    private String CardPresent;
    @JacksonXmlProperty
    private String PartialAuthFlag;
    @JacksonXmlProperty
    private String SAF;
    @JacksonXmlProperty
    private String SiteId;
    @JacksonXmlProperty
    private String WSNo;
    @JacksonXmlProperty
    private String Operator;
    @JacksonXmlProperty
    private String GuestNo;
    @JacksonXmlProperty
    private String ChargeInfo;
    @JacksonXmlProperty
    private String IndustryCode;

    @JacksonXmlProperty
    private String CheckInDate;
    @JacksonXmlProperty
    private String CheckOutDate;

    @JacksonXmlProperty
    private String ProxyInfo;
    @JacksonXmlProperty
    private String POSInfo;

    public TransactionRequest() {
    }

    public TransactionRequest(String sequenceNo, String transType, String transAmount, String transCurrency, String transToken, String issuerId, String PAN, String expiryDate, String taxAmount, String transDateTime, String cardPresent, String partialAuthFlag, String SAF, String siteId, String WSNo, String operator, String guestNo, String chargeInfo, String industryCode, String proxyInfo, String POSInfo) {
        SequenceNo = sequenceNo;
        TransType = transType;
        TransAmount = transAmount;
        TransCurrency = transCurrency;
        TransToken = transToken;
        IssuerId = issuerId;
        this.PAN = PAN;
        ExpiryDate = expiryDate;
        TaxAmount = taxAmount;
        TransDateTime = transDateTime;
        CardPresent = cardPresent;
        PartialAuthFlag = partialAuthFlag;
        this.SAF = SAF;
        SiteId = siteId;
        this.WSNo = WSNo;
        Operator = operator;
        GuestNo = guestNo;
        ChargeInfo = chargeInfo;
        IndustryCode = industryCode;
        ProxyInfo = proxyInfo;
        this.POSInfo = POSInfo;
    }

    public String getSequenceNo() {
        return SequenceNo;
    }

    public void setSequenceNo(String sequenceNo) {
        SequenceNo = sequenceNo;
    }

    public String getTransType() {
        return TransType;
    }

    public void setTransType(String transType) {
        TransType = transType;
    }

    public String getTransAmount() {
        return TransAmount;
    }

    public void setTransAmount(String transAmount) {
        TransAmount = transAmount;
    }

    public String getTransCurrency() {
        return TransCurrency;
    }

    public void setTransCurrency(String transCurrency) {
        TransCurrency = transCurrency;
    }

    public String getTransToken() {
        return TransToken;
    }

    public void setTransToken(String transToken) {
        TransToken = transToken;
    }

    public String getIssuerId() {
        return IssuerId;
    }

    public void setIssuerId(String issuerId) {
        IssuerId = issuerId;
    }

    public String getPAN() {
        return PAN;
    }

    public void setPAN(String PAN) {
        this.PAN = PAN;
    }

    public String getExpiryDate() {
        return ExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        ExpiryDate = expiryDate;
    }

    public String getTaxAmount() {
        return TaxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        TaxAmount = taxAmount;
    }

    public String getTransDateTime() {
        return TransDateTime;
    }

    public void setTransDateTime(String transDateTime) {
        TransDateTime = transDateTime;
    }

    public String getCardPresent() {
        return CardPresent;
    }

    public void setCardPresent(String cardPresent) {
        CardPresent = cardPresent;
    }

    public String getPartialAuthFlag() {
        return PartialAuthFlag;
    }

    public void setPartialAuthFlag(String partialAuthFlag) {
        PartialAuthFlag = partialAuthFlag;
    }

    public String getSAF() {
        return SAF;
    }

    public void setSAF(String SAF) {
        this.SAF = SAF;
    }

    public String getSiteId() {
        return SiteId;
    }

    public void setSiteId(String siteId) {
        SiteId = siteId;
    }

    public String getWSNo() {
        return WSNo;
    }

    public void setWSNo(String WSNo) {
        this.WSNo = WSNo;
    }

    public String getOperator() {
        return Operator;
    }

    public void setOperator(String operator) {
        Operator = operator;
    }

    public String getGuestNo() {
        return GuestNo;
    }

    public void setGuestNo(String guestNo) {
        GuestNo = guestNo;
    }

    public String getChargeInfo() {
        return ChargeInfo;
    }

    public void setChargeInfo(String chargeInfo) {
        ChargeInfo = chargeInfo;
    }

    public String getIndustryCode() {
        return IndustryCode;
    }

    public void setIndustryCode(String industryCode) {
        IndustryCode = industryCode;
    }

    public String getCheckInDate() {
        return CheckInDate;
    }

    public void setCheckInDate(String checkInDate) {
        CheckInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return CheckOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        CheckOutDate = checkOutDate;
    }

    public String getProxyInfo() {
        return ProxyInfo;
    }

    public void setProxyInfo(String proxyInfo) {
        ProxyInfo = proxyInfo;
    }

    public String getPOSInfo() {
        return POSInfo;
    }

    public void setPOSInfo(String POSInfo) {
        this.POSInfo = POSInfo;
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "SequenceNo='" + SequenceNo + '\'' +
                ", TransType='" + TransType + '\'' +
                ", TransAmount='" + TransAmount + '\'' +
                ", TransCurrency='" + TransCurrency + '\'' +
                ", TransToken='" + TransToken + '\'' +
                ", IssuerId='" + IssuerId + '\'' +
                ", PAN='" + PAN + '\'' +
                ", ExpiryDate='" + ExpiryDate + '\'' +
                ", TaxAmount='" + TaxAmount + '\'' +
                ", TransDateTime='" + TransDateTime + '\'' +
                ", CardPresent='" + CardPresent + '\'' +
                ", PartialAuthFlag='" + PartialAuthFlag + '\'' +
                ", SAF='" + SAF + '\'' +
                ", SiteId='" + SiteId + '\'' +
                ", WSNo='" + WSNo + '\'' +
                ", Operator='" + Operator + '\'' +
                ", GuestNo='" + GuestNo + '\'' +
                ", ChargeInfo='" + ChargeInfo + '\'' +
                ", IndustryCode='" + IndustryCode + '\'' +
                ", ProxyInfo='" + ProxyInfo + '\'' +
                ", POSInfo='" + POSInfo + '\'' +
                '}';
    }
}

package com.sun.supplierpoc.models.opera;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "purchaseCode",
        "customerCard",
        "attendantCard",
        "hotelID",
        "transactionMode",
        "transactionDate",
        "paiementmode",
        "transactionType",
        "transactionSource",
        "items"
})
public class Transaction {

    @JsonProperty("purchaseCode")
    private String purchaseCode;
    @JsonProperty("customerCard")
    private String customerCard;
    @JsonProperty("attendantCard")
    private String attendantCard;
    @JsonProperty("hotelID")
    private String hotelID;
    @JsonProperty("transactionMode")
    private String transactionMode;
    @JsonProperty("transactionDate")
    private String transactionDate;
    @JsonProperty("paiementmode")
    private String paiementmode;
    @JsonProperty("transactionType")
    private String transactionType;
    @JsonProperty("transactionSource")
    private String transactionSource;
    @JsonProperty("items")
    private List<Item> items = null;

    public Transaction() {
    }

    public Transaction(String purchaseCode, String customerCard, String attendantCard, String hotelID,
                       String transactionMode, String transactionDate, String paiementmode, String transactionType,
                       String transactionSource, List<Item> items){
    this.attendantCard = attendantCard;
    this.customerCard = customerCard;
    this.transactionDate = transactionDate;
    this.transactionType = transactionType;
    this.transactionMode = transactionMode;
    this.hotelID = hotelID;
    this.purchaseCode = purchaseCode;
    this.paiementmode = paiementmode;
    this.transactionSource = transactionSource;
    this.items = items;
    }

    @JsonProperty("purchaseCode")
    public String getPurchaseCode() {
        return purchaseCode;
    }

    @JsonProperty("purchaseCode")
    public void setPurchaseCode(String purchaseCode) {
        this.purchaseCode = purchaseCode;
    }

    @JsonProperty("customerCard")
    public String getCustomerCard() {
        return customerCard;
    }

    @JsonProperty("customerCard")
    public void setCustomerCard(String customerCard) {
        this.customerCard = customerCard;
    }

    @JsonProperty("attendantCard")
    public String getAttendantCard() {
        return attendantCard;
    }

    @JsonProperty("attendantCard")
    public void setAttendantCard(String attendantCard) {
        this.attendantCard = attendantCard;
    }

    @JsonProperty("hotelID")
    public String getHotelID() {
        return hotelID;
    }

    @JsonProperty("hotelID")
    public void setHotelID(String hotelID) {
        this.hotelID = hotelID;
    }

    @JsonProperty("transactionMode")
    public String getTransactionMode() {
        return transactionMode;
    }

    @JsonProperty("transactionMode")
    public void setTransactionMode(String transactionMode) {
        this.transactionMode = transactionMode;
    }

    @JsonProperty("transactionDate")
    public String getTransactionDate() {
        return transactionDate;
    }

    @JsonProperty("transactionDate")
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @JsonProperty("paiementmode")
    public String getPaiementmode() {
        return paiementmode;
    }

    @JsonProperty("paiementmode")
    public void setPaiementmode(String paiementmode) {
        this.paiementmode = paiementmode;
    }

    @JsonProperty("transactionType")
    public String getTransactionType() {
        return transactionType;
    }

    @JsonProperty("transactionType")
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @JsonProperty("transactionSource")
    public String getTransactionSource() {
        return transactionSource;
    }

    @JsonProperty("transactionSource")
    public void setTransactionSource(String transactionSource) {
        this.transactionSource = transactionSource;
    }

    @JsonProperty("items")
    public List<Item> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<Item> items) {
        this.items = items;
    }


}

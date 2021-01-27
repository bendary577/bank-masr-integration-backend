package com.sun.supplierpoc.models.opera;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.io.Serializable;

@JsonTypeName(value = "cardInfo")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class TransactionObjectResponse implements Serializable {
    public boolean status = false;

    public String message = "";

    public String authorizationNumber = "";

    public String cardLastDigits = "";

    public TransactionObjectResponse() {
    }

    public TransactionObjectResponse(String cardLastDigits, String authorizationNumber) {
        this.cardLastDigits = cardLastDigits;
        this.authorizationNumber = authorizationNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TransactionObjectResponse(String cardLastDigits) {
        this.cardLastDigits = cardLastDigits;
    }

    public String getCardLastDigits() {
        return cardLastDigits;
    }

    public void setCardLastDigits(String cardLastDigits) {
        this.cardLastDigits = cardLastDigits;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }
}

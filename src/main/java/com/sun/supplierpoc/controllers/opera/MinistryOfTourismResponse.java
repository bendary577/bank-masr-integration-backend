package com.sun.supplierpoc.controllers.opera;

import java.util.ArrayList;
import java.util.List;

public class MinistryOfTourismResponse {
    protected List<String> errorCode;
    protected String correlationId;
    protected String transactionId;

    public MinistryOfTourismResponse() {
    }

    public MinistryOfTourismResponse(List<String> errorCode, String correlationId, String transactionId) {
        this.errorCode = errorCode;
        this.correlationId = correlationId;
        this.transactionId = transactionId;
    }

    public List<String> getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(List<String> errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorCode(ArrayList<String> errorCode) {
        this.errorCode = errorCode;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}

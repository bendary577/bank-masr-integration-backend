package com.sun.supplierpoc.controllers.opera;

import java.util.ArrayList;
import java.util.List;

public class MinistryOfTourismResponse {
    protected List<String> errorCode;
    protected String correlationId;

    public MinistryOfTourismResponse() {
    }

    public MinistryOfTourismResponse(ArrayList<String> errorCode, String correlationId) {
        this.errorCode = errorCode;
        this.correlationId = correlationId;
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
}

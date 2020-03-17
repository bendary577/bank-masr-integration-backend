package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class Analysis implements Serializable {
    private boolean checked;
    private String number;
    private String codeElement;
    private String reference;

    public Analysis() {
    }

    public Analysis(boolean checked, String number, String codeElement, String reference) {
        this.checked = checked;
        this.number = number;
        this.codeElement = codeElement;
        this.reference = reference;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCodeElement() {
        return codeElement;
    }

    public void setCodeElement(String codeElement) {
        this.codeElement = codeElement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

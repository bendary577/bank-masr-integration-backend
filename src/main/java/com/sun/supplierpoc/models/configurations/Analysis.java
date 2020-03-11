package com.sun.supplierpoc.models.configurations;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

public class Analysis implements Serializable {

    @Id
    private String id;
    private boolean checked;
    private String number;
    private String codeElement;
    private String reference;

    public Analysis() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

package com.sun.supplierpoc.models.simphony;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "string"
})
public class PCheckInfoLines {

    @XmlElement(name = "string")
    @NotBlank(message = "line can't be blank.")
    @NotNull(message = "line can't be null.")
    private String string;

    public String getString() {
        return string;
    }

    @JsonProperty(value = "line")
    public void setString(String string) {
        this.string = string;
    }
}

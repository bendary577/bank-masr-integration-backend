package com.sun.supplierpoc.models.simphony.check;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
@Document(collection = "zealPayment")
public class ZealPayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;
    private String code;
    private String totalDue;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(String totalDue) {
        this.totalDue = totalDue;
    }
}

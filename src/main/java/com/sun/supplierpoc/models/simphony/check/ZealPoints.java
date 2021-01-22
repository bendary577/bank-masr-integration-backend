package com.sun.supplierpoc.models.simphony.check;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zealPoints")
public class ZealPoints {

    @Id
    private int id;
    private String code;
    private String totalDue;

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

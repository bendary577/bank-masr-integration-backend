package com.sun.supplierpoc.models.simphony.check;

import com.sun.supplierpoc.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zealPoints")
public class ZealPoints {

    @Id
    private int id;
    private String code = "-";
    private String totalDue = "-";
    private String message = "-";
    private String status = Constants.FAILED;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setTotalDue(String totalDue) {
        this.totalDue = totalDue;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

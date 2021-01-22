package com.sun.supplierpoc.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@Document(collection = "operationData")
public class OperationData implements Serializable {

    @Id
    private String id;
    private HashMap<String, Object> data;
    private String status;
    private String reason;
    private Date creationDate;
    private String operationId;
    private boolean deleted;

    public OperationData(HashMap<String, Object> data, String status, String reason, Date creationDate, String operationId) {
        this.data = data;
        this.status = status;
        this.reason = reason;
        this.creationDate = creationDate;
        this.operationId = operationId;
    }
}

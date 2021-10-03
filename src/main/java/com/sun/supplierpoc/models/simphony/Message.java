package com.sun.supplierpoc.models.simphony;

public class Message {

    private String ar;
    private String en;

    public Message() {
    }

    public Message(String en) {
        this.en = en;
    }

    public String getAr() {
        return ar;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }
}

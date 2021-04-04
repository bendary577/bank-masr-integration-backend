package com.sun.supplierpoc.models.opera.booking;

public class BookingType {
    private boolean checked = false;
    private String typeId = "";
    private String type = "";
    private String typeDescription = "";

    public BookingType() { }

    public BookingType(String typeId) {
        this.typeId = typeId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(String typeDescription) {
        this.typeDescription = typeDescription;
    }
}

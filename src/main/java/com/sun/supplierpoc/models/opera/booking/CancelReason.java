package com.sun.supplierpoc.models.opera.booking;

public class CancelReason {
    private boolean checked = false;
    private String reasonId = "";
    private String reason = "";
    private String reasonDescription = "";

    public CancelReason() {
    }

    public CancelReason(String reasonId) {
        this.reasonId = reasonId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getReasonId() {
        return reasonId;
    }

    public void setReasonId(String reasonId) {
        this.reasonId = reasonId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }
}

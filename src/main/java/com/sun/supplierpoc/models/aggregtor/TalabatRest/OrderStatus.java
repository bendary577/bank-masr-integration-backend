package com.sun.supplierpoc.models.aggregtor.TalabatRest;

public class OrderStatus {

    private String status;
    private String timestamp;
    private String billable;
    private SentToTransmissionDetails sentToTransmissionDetails;
    private Metadata metadata;
    private SendingToVendorDetails sendingToVendorDetails;
    private DisplayedAtVendorDetails displayedAtVendorDetails;
    private AcceptedDetails acceptedDetails;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBillable() {
        return billable;
    }

    public void setBillable(String billable) {
        this.billable = billable;
    }

    public SentToTransmissionDetails getSentToTransmissionDetails() {
        return sentToTransmissionDetails;
    }

    public void setSentToTransmissionDetails(SentToTransmissionDetails sentToTransmissionDetails) {
        this.sentToTransmissionDetails = sentToTransmissionDetails;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public SendingToVendorDetails getSendingToVendorDetails() {
        return sendingToVendorDetails;
    }

    public void setSendingToVendorDetails(SendingToVendorDetails sendingToVendorDetails) {
        this.sendingToVendorDetails = sendingToVendorDetails;
    }

    public DisplayedAtVendorDetails getDisplayedAtVendorDetails() {
        return displayedAtVendorDetails;
    }

    public void setDisplayedAtVendorDetails(DisplayedAtVendorDetails displayedAtVendorDetails) {
        this.displayedAtVendorDetails = displayedAtVendorDetails;
    }

    public AcceptedDetails getAcceptedDetails() {
        return acceptedDetails;
    }

    public void setAcceptedDetails(AcceptedDetails acceptedDetails) {
        this.acceptedDetails = acceptedDetails;
    }

}

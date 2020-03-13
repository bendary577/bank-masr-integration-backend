package com.sun.supplierpoc.models.data;

public class Invoice extends Data {
    // Invoice Data
    private String invoiceNo;
    private String invoiceDate;
    private String vendor;
    private String costCenter;
    private String gross;
    private String net;
    private String vat;
    private String createdBy;
    private String createdAt;
    private String status;

    public Invoice() {
        super();
    }

    public Invoice(String invoiceNo, String invoiceDate, String vendor, String costCenter, String gross, String net, String vat, String createdBy, String createdAt, String status) {
        super();
        this.invoiceNo = invoiceNo;
        this.invoiceDate = invoiceDate;
        this.vendor = vendor;
        this.costCenter = costCenter;
        this.gross = gross;
        this.net = net;
        this.vat = vat;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.status = status;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    public String getNet() {
        return net;
    }

    public void setNet(String net) {
        this.net = net;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

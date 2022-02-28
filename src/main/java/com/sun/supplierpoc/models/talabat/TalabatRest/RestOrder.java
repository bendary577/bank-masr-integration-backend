package com.sun.supplierpoc.models.talabat.TalabatRest;

public class RestOrder {
    public String identifier;
    public String order_id;
    public String global_vendor_code;
    public String vendor_name;
    public String order_status;
    public String order_timestamp;
    public String billable_status;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getGlobal_vendor_code() {
        return global_vendor_code;
    }

    public void setGlobal_vendor_code(String global_vendor_code) {
        this.global_vendor_code = global_vendor_code;
    }

    public String getVendor_name() {
        return vendor_name;
    }

    public void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getOrder_timestamp() {
        return order_timestamp;
    }

    public void setOrder_timestamp(String order_timestamp) {
        this.order_timestamp = order_timestamp;
    }

    public String getBillable_status() {
        return billable_status;
    }

    public void setBillable_status(String billable_status) {
        this.billable_status = billable_status;
    }

}
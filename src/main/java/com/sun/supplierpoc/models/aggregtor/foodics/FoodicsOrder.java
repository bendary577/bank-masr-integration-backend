package com.sun.supplierpoc.models.aggregtor.foodics;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;


@Document
public class FoodicsOrder {

    @Id
    @NotNull(message = "ID can't be null.")
    private String id;
    private Integer type;
    private String branchId;

    private String customerAddressId;
    private String customerId;
    private String customerName;
    private String customerDialCode;
    private String customerPhone;
    private String customerAddressName;
    private String customerAddressDescription;
    private String customerAddressLongitude;
    private String customerAddressLatitude;

    private List<Charge> charges = null;
    private List<FoodicsProductObject> products = null;

    private double discount_amount;
    private int discount_type;

    private String check_number;

    @JsonProperty("order_status")
    private int status = 1;
    private boolean callStatus;
    private int delivery_status = 1;
    private String message;
    private Object errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerDialCode() {
        return customerDialCode;
    }

    public void setCustomerDialCode(String customerDialCode) {
        this.customerDialCode = customerDialCode;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerAddressName() {
        return customerAddressName;
    }

    public void setCustomerAddressName(String customerAddressName) {
        this.customerAddressName = customerAddressName;
    }

    public String getCustomerAddressDescription() {
        return customerAddressDescription;
    }

    public void setCustomerAddressDescription(String customerAddressDescription) {
        this.customerAddressDescription = customerAddressDescription;
    }

    public String getCustomerAddressLongitude() {
        return customerAddressLongitude;
    }

    public void setCustomerAddressLongitude(String customerAddressLongitude) {
        this.customerAddressLongitude = customerAddressLongitude;
    }

    public String getCustomerAddressLatitude() {
        return customerAddressLatitude;
    }

    public void setCustomerAddressLatitude(String customerAddressLatitude) {
        this.customerAddressLatitude = customerAddressLatitude;
    }

    public String getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(String customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<Charge> getCharges() {
        return charges;
    }

    public void setCharges(List<Charge> charges) {
        this.charges = charges;
    }

    public List<FoodicsProductObject> getProducts() {
        return products;
    }

    public void setProducts(List<FoodicsProductObject> products) {
        this.products = products;
    }

    public int getStatus() {
        return status;
    }

    public double getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(double discount_amount) {
        this.discount_amount = discount_amount;
    }

    public int getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(int discount_type) {
        this.discount_type = discount_type;
    }

    public String getCheck_number() {
        return check_number;
    }

    public void setCheck_number(String check_number) {
        this.check_number = check_number;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isCallStatus() {
        return callStatus;
    }

    public void setCallStatus(boolean callStatus) {
        this.callStatus = callStatus;
    }

    public int getDelivery_status() {
        return delivery_status;
    }

    public void setDelivery_status(int delivery_status) {
        this.delivery_status = delivery_status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }
}

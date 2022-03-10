package com.sun.supplierpoc.models.aggregtor.TalabatRest;

import java.util.ArrayList;
import java.util.Date;

public class TalabatAdminOrder {

    public String id;
    public Date timestamp;
    public String state;
    public String dispatchStateType;
    public String trackingStateType;
    public String platformKey;
    public String globalEntityId;
    public String externalRestaurantId;
    public String vendorName;
    public String externalId;
    public boolean test;
    public boolean preorder;
    public boolean guaranteed;
    public Transport transport;
    public Date seenAt;
    public Date deliverAt;
    public Date expiresAt;
    public Date promisedTime;
    public Date acceptedAt;
    public Customer customer;
    public Address address;
    public Payment payment;
    public ArrayList<Item> items;
    public ArrayList<Tax> taxes;
    public boolean canVoid;
    public boolean canDelay;
    public boolean corporate;
    public String shortCode;
    public boolean preparationCompleted;
    public boolean preparationCompletionSupported;
    public String accepter;
    public String logisticsProviderId;
    public String vendorTimeZone;
    public String platformName;
    public boolean acknowledged;
    public String itemUnavailabilityHandling;
    public Object vendorExtraParameters;
    private boolean status;
    private String message;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDispatchStateType() {
        return dispatchStateType;
    }

    public void setDispatchStateType(String dispatchStateType) {
        this.dispatchStateType = dispatchStateType;
    }

    public String getTrackingStateType() {
        return trackingStateType;
    }

    public void setTrackingStateType(String trackingStateType) {
        this.trackingStateType = trackingStateType;
    }

    public String getPlatformKey() {
        return platformKey;
    }

    public void setPlatformKey(String platformKey) {
        this.platformKey = platformKey;
    }

    public String getGlobalEntityId() {
        return globalEntityId;
    }

    public void setGlobalEntityId(String globalEntityId) {
        this.globalEntityId = globalEntityId;
    }

    public String getExternalRestaurantId() {
        return externalRestaurantId;
    }

    public void setExternalRestaurantId(String externalRestaurantId) {
        this.externalRestaurantId = externalRestaurantId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isPreorder() {
        return preorder;
    }

    public void setPreorder(boolean preorder) {
        this.preorder = preorder;
    }

    public boolean isGuaranteed() {
        return guaranteed;
    }

    public void setGuaranteed(boolean guaranteed) {
        this.guaranteed = guaranteed;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Date getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(Date seenAt) {
        this.seenAt = seenAt;
    }

    public Date getDeliverAt() {
        return deliverAt;
    }

    public void setDeliverAt(Date deliverAt) {
        this.deliverAt = deliverAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Date getPromisedTime() {
        return promisedTime;
    }

    public void setPromisedTime(Date promisedTime) {
        this.promisedTime = promisedTime;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<Tax> getTaxes() {
        return taxes;
    }

    public void setTaxes(ArrayList<Tax> taxes) {
        this.taxes = taxes;
    }

    public boolean isCanVoid() {
        return canVoid;
    }

    public void setCanVoid(boolean canVoid) {
        this.canVoid = canVoid;
    }

    public boolean isCanDelay() {
        return canDelay;
    }

    public void setCanDelay(boolean canDelay) {
        this.canDelay = canDelay;
    }

    public boolean isCorporate() {
        return corporate;
    }

    public void setCorporate(boolean corporate) {
        this.corporate = corporate;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public boolean isPreparationCompleted() {
        return preparationCompleted;
    }

    public void setPreparationCompleted(boolean preparationCompleted) {
        this.preparationCompleted = preparationCompleted;
    }

    public boolean isPreparationCompletionSupported() {
        return preparationCompletionSupported;
    }

    public void setPreparationCompletionSupported(boolean preparationCompletionSupported) {
        this.preparationCompletionSupported = preparationCompletionSupported;
    }

    public String getAccepter() {
        return accepter;
    }

    public void setAccepter(String accepter) {
        this.accepter = accepter;
    }

    public String getLogisticsProviderId() {
        return logisticsProviderId;
    }

    public void setLogisticsProviderId(String logisticsProviderId) {
        this.logisticsProviderId = logisticsProviderId;
    }

    public String getVendorTimeZone() {
        return vendorTimeZone;
    }

    public void setVendorTimeZone(String vendorTimeZone) {
        this.vendorTimeZone = vendorTimeZone;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getItemUnavailabilityHandling() {
        return itemUnavailabilityHandling;
    }

    public void setItemUnavailabilityHandling(String itemUnavailabilityHandling) {
        this.itemUnavailabilityHandling = itemUnavailabilityHandling;
    }

    public Object getVendorExtraParameters() {
        return vendorExtraParameters;
    }

    public void setVendorExtraParameters(Object vendorExtraParameters) {
        this.vendorExtraParameters = vendorExtraParameters;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}

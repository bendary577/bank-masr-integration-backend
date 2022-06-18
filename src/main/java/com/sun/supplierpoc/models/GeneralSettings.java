package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.applications.BirthdayGift;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.applications.SimphonyQuota;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.opera.PosMachineMap;
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.RateCode;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class GeneralSettings {

    @Id
    private String id;
    private String accountId;

    private ArrayList<RevenueCenter> revenueCenters = new ArrayList<>();
    private ArrayList<SimphonyLocation> simphonyLocations = new ArrayList<>();

    /* Opera Configurations */
    private ArrayList<BookingType> cancelReasons = new ArrayList<>();
    private ArrayList<BookingType> paymentTypes = new ArrayList<>();
    private ArrayList<BookingType> roomTypes = new ArrayList<>();
    private ArrayList<BookingType> nationalities = new ArrayList<>();
    private ArrayList<BookingType> purposeOfVisit = new ArrayList<>();
    private ArrayList<BookingType> genders = new ArrayList<>();
    private ArrayList<BookingType> customerTypes = new ArrayList<>();
    private ArrayList<BookingType> transactionTypes = new ArrayList<>();
    private ArrayList<BookingType> expenseTypes = new ArrayList<>();
    private ArrayList<RateCode> rateCodes = new ArrayList<>();
    private ArrayList<PosMachineMap> posMachineMaps = new ArrayList<>();
    private ArrayList<SimphonyDiscount> discountRates = new ArrayList<>();
    private boolean discountAppliedAfterFess = false;


    private ArrayList<OrderType> orderTypes = new ArrayList<>();
    private SimphonyQuota simphonyQuota;
    private String mailSubj;

    /* Reward Points */
    private float pointReward = 0; // percentage
    private float pointsRedemption = 0; // 1$ = ? points
    private BirthdayGift birthdayGift = new BirthdayGift();

    private Date creationDate;
    private boolean deleted;



    public GeneralSettings() {
    }

    public GeneralSettings(String accountId, Date creationDate) {
        this.accountId = accountId;
        this.creationDate = creationDate;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }



    public ArrayList<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(ArrayList<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ArrayList<SimphonyLocation> getSimphonyLocations() {
        return simphonyLocations;
    }

    public void setSimphonyLocations(ArrayList<SimphonyLocation> simphonyLocations) {
        this.simphonyLocations = simphonyLocations;
    }

    public SimphonyLocation getSimphonyLocationsByID(int revenueCenterID){
        for (SimphonyLocation location : this.simphonyLocations) {
            if (location.getRevenueCenterID() == revenueCenterID) {
                return location;
            }
        }
        return null;
    }

    public ArrayList<SimphonyDiscount> getDiscountRates() {
        return discountRates;
    }

    public void setDiscountRates(ArrayList<SimphonyDiscount> discountRates) {
        this.discountRates = discountRates;
    }

    public boolean isDiscountAppliedAfterFess() {
        return discountAppliedAfterFess;
    }

    public void setDiscountAppliedAfterFess(boolean discountAppliedAfterFess) {
        this.discountAppliedAfterFess = discountAppliedAfterFess;
    }

    public ArrayList<BookingType> getPaymentTypes() {
        return paymentTypes;
    }

    public void setPaymentTypes(ArrayList<BookingType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public ArrayList<BookingType> getCancelReasons() {
        return cancelReasons;
    }

    public void setCancelReasons(ArrayList<BookingType> cancelReasons) {
        this.cancelReasons = cancelReasons;
    }

    public ArrayList<BookingType> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(ArrayList<BookingType> roomTypes) {
        this.roomTypes = roomTypes;
    }

    public ArrayList<BookingType> getNationalities() {
        return nationalities;
    }

    public void setNationalities(ArrayList<BookingType> nationalities) {
        this.nationalities = nationalities;
    }

    public ArrayList<BookingType> getPurposeOfVisit() {
        return purposeOfVisit;
    }

    public void setPurposeOfVisit(ArrayList<BookingType> purposeOfVisit) {
        this.purposeOfVisit = purposeOfVisit;
    }

    public ArrayList<BookingType> getGenders() {
        return genders;
    }

    public void setGenders(ArrayList<BookingType> genders) {
        this.genders = genders;
    }

    public ArrayList<BookingType> getCustomerTypes() {
        return customerTypes;
    }

    public void setCustomerTypes(ArrayList<BookingType> customerTypes) {
        this.customerTypes = customerTypes;
    }

    public ArrayList<BookingType> getTransactionTypes() {
        return transactionTypes;
    }

    public void setTransactionTypes(ArrayList<BookingType> transactionTypes) {
        this.transactionTypes = transactionTypes;
    }

    public ArrayList<BookingType> getExpenseTypes() {
        return expenseTypes;
    }

    public void setExpenseTypes(ArrayList<BookingType> expenseTypes) {
        this.expenseTypes = expenseTypes;
    }

    public ArrayList<RateCode> getRateCodes() {
        return rateCodes;
    }

    public void setRateCodes(ArrayList<RateCode> rateCodes) {
        this.rateCodes = rateCodes;
    }

    public SimphonyQuota getSimphonyQuota() {
        return simphonyQuota;
    }

    public void setSimphonyQuota(SimphonyQuota simphonyQuota) {
        this.simphonyQuota = simphonyQuota;
    }

    public String getMailSub() {
        return mailSubj;
    }


    public void setMailSub(String mailSubj) {
        this.mailSubj = mailSubj;
    }

    public ArrayList<PosMachineMap> getPosMachineMaps() {
        return posMachineMaps;
    }

    public void setPosMachineMaps(ArrayList<PosMachineMap> posMachineMaps) {
        this.posMachineMaps = posMachineMaps;
    }

    public String getMailSubj() {
        return mailSubj;
    }

    public void setMailSubj(String mailSubj) {
        this.mailSubj = mailSubj;
    }

    public float getPointReward() {
        return pointReward;
    }

    public void setPointReward(float pointReward) {
        this.pointReward = pointReward;
    }

    public float getPointsRedemption() {
        return pointsRedemption;
    }

    public void setPointsRedemption(float pointsRedemption) {
        this.pointsRedemption = pointsRedemption;
    }

    public BirthdayGift getBirthdayGift() {
        return birthdayGift;
    }

    public void setBirthdayGift(BirthdayGift birthdayGift) {
        this.birthdayGift = birthdayGift;
    }


    public ArrayList<OrderType> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(ArrayList<OrderType> orderTypes) {
        this.orderTypes = orderTypes;
    }


}



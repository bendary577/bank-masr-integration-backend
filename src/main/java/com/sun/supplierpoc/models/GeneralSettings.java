package com.sun.supplierpoc.models;

import com.sun.supplierpoc.models.applications.BirthdayGift;
import com.sun.supplierpoc.models.applications.SimphonyDiscount;
import com.sun.supplierpoc.models.applications.SimphonyQuota;
import com.sun.supplierpoc.models.configurations.*;
import com.sun.supplierpoc.models.opera.PosMachineMap;
import com.sun.supplierpoc.models.opera.booking.BookingType;
import com.sun.supplierpoc.models.opera.booking.RateCode;
import com.sun.supplierpoc.models.talabat.BranchMapping;
import com.sun.supplierpoc.models.talabat.ComboMapping;
import com.sun.supplierpoc.models.talabat.ProductsMapping;
import com.sun.supplierpoc.soapModels.Supplier;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;

public class GeneralSettings {

    @Id
    private String id;
    private String accountId;

    private ArrayList<Item> items = new ArrayList<>();
    private ArrayList<ItemGroup> itemGroups = new ArrayList<>();
    private ArrayList<MajorGroup> majorGroups = new ArrayList<>();
    private ArrayList<OverGroup> overGroups = new ArrayList<>();
    private ArrayList<CostCenter> costCenterAccountMapping = new ArrayList<>();
    private ArrayList<CostCenter> locations = new ArrayList<>();
    private ArrayList<RevenueCenter> revenueCenters = new ArrayList<>();
    private ArrayList<SimphonyLocation> simphonyLocations = new ArrayList<>();
    private ArrayList<Supplier> suppliers = new ArrayList<>();

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
    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<ComboMapping> comboMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();

    private SimphonyQuota simphonyQuota;
    private String mailSubj;

    /* Reword Points */
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

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public ArrayList<ItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(ArrayList<ItemGroup> itemGroups) {
        this.itemGroups = itemGroups;
    }

    public ArrayList<MajorGroup> getMajorGroups() {
        return majorGroups;
    }

    public void setMajorGroups(ArrayList<MajorGroup> majorGroups) {
        this.majorGroups = majorGroups;
    }

    public ArrayList<OverGroup> getOverGroups() {
        return overGroups;
    }

    public void setOverGroups(ArrayList<OverGroup> overGroups) {
        this.overGroups = overGroups;
    }

    public ArrayList<CostCenter> getCostCenterAccountMapping() {
        return costCenterAccountMapping;
    }

    public void setCostCenterAccountMapping(ArrayList<CostCenter> costCenterAccountMapping) {
        this.costCenterAccountMapping = costCenterAccountMapping;
    }

    public ArrayList<RevenueCenter> getRevenueCenters() {
        return revenueCenters;
    }

    public void setRevenueCenters(ArrayList<RevenueCenter> revenueCenters) {
        this.revenueCenters = revenueCenters;
    }

    public ArrayList<CostCenter> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<CostCenter> locations) {
        this.locations = locations;
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

    public ArrayList<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(ArrayList<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public ArrayList<SimphonyDiscount> getDiscountRates() {
        return discountRates;
    }

    public void setDiscountRates(ArrayList<SimphonyDiscount> discountRates) {
        this.discountRates = discountRates;
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
}



package com.sun.supplierpoc.models.configurations;

import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.*;

import java.util.ArrayList;
import java.util.List;

public class AggregatorConfiguration {

    private FoodicsAccountData foodicsAccountData = new FoodicsAccountData();
    private List<SimphonyAccount> simphonyAccountData = new ArrayList<>();
    private ArrayList<TalabatAdminAccount> talabatAdminAccounts = new ArrayList<>();
    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();
    private ArrayList<CustomerMapping> customerMappings = new ArrayList<>();
    private ArrayList<AddressMapping> addressMappings = new ArrayList<>();
    private ArrayList<DiscountMapping> discountMappings = new ArrayList<>();

    public FoodicsAccountData getFoodicsAccount() {
        return foodicsAccountData;
    }

    public void setFoodicsAccount(FoodicsAccountData foodicsAccountData) {
        this.foodicsAccountData = foodicsAccountData;
    }

    public ArrayList<ProductsMapping> getProductsMappings() {
        return productsMappings;
    }

    public void setProductsMappings(ArrayList<ProductsMapping> productsMappings) {
        this.productsMappings = productsMappings;
    }

    public ArrayList<BranchMapping> getBranchMappings() {
        return branchMappings;
    }

    public void setBranchMappings(ArrayList<BranchMapping> branchMappings) {
        this.branchMappings = branchMappings;
    }

    public ArrayList<CustomerMapping> getCustomerMappings() {
        return customerMappings;
    }

    public void setCustomerMappings(ArrayList<CustomerMapping> customerMappings) {
        this.customerMappings = customerMappings;
    }

    public ArrayList<AddressMapping> getAddressMappings() {
        return addressMappings;
    }

    public void setAddressMappings(ArrayList<AddressMapping> addressMappings) {
        this.addressMappings = addressMappings;
    }

    public ArrayList<DiscountMapping> getDiscountMappings() {
        return discountMappings;
    }

    public void setDiscountMappings(ArrayList<DiscountMapping> discountMappings) {
        this.discountMappings = discountMappings;
    }

    public ArrayList<TalabatAdminAccount> getTalabatAdminAccounts() {
        return talabatAdminAccounts;
    }

    public void setTalabatAdminAccounts(ArrayList<TalabatAdminAccount> talabatAdminAccounts) {
        this.talabatAdminAccounts = talabatAdminAccounts;
    }

    public FoodicsAccountData getFoodicsAccountData() {
        return foodicsAccountData;
    }

    public void setFoodicsAccountData(FoodicsAccountData foodicsAccountData) {
        this.foodicsAccountData = foodicsAccountData;
    }
}

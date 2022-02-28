package com.sun.supplierpoc.models.configurations;

import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.*;

import java.util.ArrayList;

public class TalabatConfiguration {

    private FoodicsAccount foodicsAccount = new FoodicsAccount();
    private ArrayList<TalabatAdminAccount> talabatAdminAccount = new ArrayList<>();
    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();
    private ArrayList<CustomerMapping> customerMappings = new ArrayList<>();
    private ArrayList<AddressMapping> addressMappings = new ArrayList<>();
    private ArrayList<DiscountMapping> discountMappings = new ArrayList<>();

    public FoodicsAccount getFoodicsAccount() {
        return foodicsAccount;
    }

    public void setFoodicsAccount(FoodicsAccount foodicsAccount) {
        this.foodicsAccount = foodicsAccount;
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

    public ArrayList<TalabatAdminAccount> getTalabatAdminAccount() {
        return talabatAdminAccount;
    }

    public void setTalabatAdminAccount(ArrayList<TalabatAdminAccount> talabatAdminAccount) {
        this.talabatAdminAccount = talabatAdminAccount;
    }
}

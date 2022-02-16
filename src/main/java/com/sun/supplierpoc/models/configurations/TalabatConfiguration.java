package com.sun.supplierpoc.models.configurations;

import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.BranchMapping;
import com.sun.supplierpoc.models.talabat.DiscountMapping;
import com.sun.supplierpoc.models.talabat.ProductsMapping;

import java.util.ArrayList;

public class TalabatConfiguration {

    private FoodicsAccount foodicsAccount = new FoodicsAccount();
    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();
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

    public ArrayList<DiscountMapping> getDiscountMappings() {
        return discountMappings;
    }

    public void setDiscountMappings(ArrayList<DiscountMapping> discountMappings) {
        this.discountMappings = discountMappings;
    }
}

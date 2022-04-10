package com.sun.supplierpoc.models.configurations;

import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.*;

import java.util.ArrayList;
import java.util.List;

public class AggregatorConfiguration {
    /* Applications */
    private ArrayList<TalabatAdminAccount> talabatAdminAccounts = new ArrayList<>();

    /* Systems */
    private FoodicsAccountData foodicsAccountData = new FoodicsAccountData();
    private SimphonyAccount simphonyAccount = new SimphonyAccount();
    private List<SimphonyAccountData> simphonyAccountData = new ArrayList<>();

    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<ModifierMapping> modifierMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();
//    private ArrayList<CustomerMapping> customerMappings = new ArrayList<>();
//    private ArrayList<AddressMapping> addressMappings = new ArrayList<>();
//    private ArrayList<DiscountMapping> discountMappings = new ArrayList<>();

    public FoodicsAccountData getFoodicsAccount() {
        return foodicsAccountData;
    }

    public SimphonyAccount getSimphonyAccount() {
        return simphonyAccount;
    }

    public void setSimphonyAccount(SimphonyAccount simphonyAccount) {
        this.simphonyAccount = simphonyAccount;
    }

    public List<SimphonyAccountData> getSimphonyAccountData() {
        return simphonyAccountData;
    }

    public void setSimphonyAccountData(List<SimphonyAccountData> simphonyAccountData) {
        this.simphonyAccountData = simphonyAccountData;
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

    public ArrayList<ModifierMapping> getModifierMappings() {
        return modifierMappings;
    }

    public void setModifierMappings(ArrayList<ModifierMapping> modifierMappings) {
        this.modifierMappings = modifierMappings;
    }

    public boolean checkProductMappingExistence(ArrayList<ProductsMapping> productsMappings, String productTalabatId){
        for (ProductsMapping product : productsMappings) {
            if(product.getTalabatProductId().equals(productTalabatId))
                return true;
        }
        return false;
    }

    public boolean checkModifierExistence(ArrayList<ModifierMapping> modifierMappings, String modifierTalabatId){
        for (ModifierMapping modifierMapping : modifierMappings) {
            if(modifierMapping.getTalabatProductId().equals(modifierTalabatId))
                return true;
        }
        return false;
    }
}

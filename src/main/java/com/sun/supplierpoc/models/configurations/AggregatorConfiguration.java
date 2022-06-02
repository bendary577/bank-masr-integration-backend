package com.sun.supplierpoc.models.configurations;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsBranch;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsModifier;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsProduct;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.*;
import com.sun.supplierpoc.models.configurations.talabat.TalabatAccountData;

import java.util.ArrayList;
import java.util.List;

public class AggregatorConfiguration {
    /* Applications */
    private ArrayList<TalabatAdminAccount> talabatAdminAccounts = new ArrayList<>();

    /* Systems */
    private FoodicsAccountData foodicsAccountData = new FoodicsAccountData();
    private TalabatAccountData talabatAccountData = new TalabatAccountData();
    private SimphonyAccount simphonyAccount = new SimphonyAccount();
    private List<SimphonyAccountData> simphonyAccountData = new ArrayList<>();

    private ArrayList<ProductsMapping> productsMappings = new ArrayList<>();
    private ArrayList<ProductsMapping> unMappedProductsMappings = new ArrayList<>();
    private ArrayList<ProductsMapping> ProductsNeedsAttention = new ArrayList<>();
    private ArrayList<ModifierMapping> modifierMappings = new ArrayList<>();
    private ArrayList<BranchMapping> branchMappings = new ArrayList<>();
    private ArrayList<FoodicsProduct> foodicsDropDownProducts = new ArrayList<>();
    private ArrayList<FoodicsModifier> foodicsDropDownModifiers = new ArrayList<>();
    private ArrayList<FoodicsBranch> foodicsDropDownBranches = new ArrayList<>();

    private boolean integrationStatus = false;
    private boolean talabatIntegrationStatus = false;
    private String mailRequiredForUpdates = "";

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

    public ArrayList<ProductsMapping> getProductsNeedsAttention() {
        return ProductsNeedsAttention;
    }

    public void setProductsNeedsAttention(ArrayList<ProductsMapping> productsNeedsAttention) {
        ProductsNeedsAttention = productsNeedsAttention;
    }

    public boolean isIntegrationStatus() {
        return integrationStatus;
    }

    public void setIntegrationStatus(boolean integrationStatus) {
        this.integrationStatus = integrationStatus;
    }

    public boolean isTalabatIntegrationStatus() {
        return talabatIntegrationStatus;
    }

    public void setTalabatIntegrationStatus(boolean talabatIntegrationStatus) {
        this.talabatIntegrationStatus = talabatIntegrationStatus;
    }

    public String getMailRequiredForUpdates() {
        return mailRequiredForUpdates;
    }

    public void setMailRequiredForUpdates(String mailRequiredForUpdates) {
        this.mailRequiredForUpdates = mailRequiredForUpdates;
    }

    public ArrayList<FoodicsProduct> getFoodicsDropDownProducts() {
        return foodicsDropDownProducts;
    }

    public void setFoodicsDropDownProducts(ArrayList<FoodicsProduct> foodicsDropDownProducts) {
        this.foodicsDropDownProducts = foodicsDropDownProducts;
    }

    public ArrayList<FoodicsModifier> getFoodicsDropDownModifiers() {
        return foodicsDropDownModifiers;
    }

    public void setFoodicsDropDownModifiers(ArrayList<FoodicsModifier> foodicsDropDownModifiers) {
        this.foodicsDropDownModifiers = foodicsDropDownModifiers;
    }

    public TalabatAccountData getTalabatAccountData() {
        return talabatAccountData;
    }

    public void setTalabatAccountData(TalabatAccountData talabatAccountData) {
        this.talabatAccountData = talabatAccountData;
    }

    public ArrayList<ProductsMapping> getUnMappedProductsMappings() {
        return unMappedProductsMappings;
    }

    public void setUnMappedProductsMappings(ArrayList<ProductsMapping> unMappedProductsMappings) {
        this.unMappedProductsMappings = unMappedProductsMappings;
    }

    public ArrayList<FoodicsBranch> getFoodicsDropDownBranches() {
        return foodicsDropDownBranches;
    }

    public void setFoodicsDropDownBranches(ArrayList<FoodicsBranch> foodicsDropDownBranches) {
        this.foodicsDropDownBranches = foodicsDropDownBranches;
    }
}

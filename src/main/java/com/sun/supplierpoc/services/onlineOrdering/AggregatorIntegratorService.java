package com.sun.supplierpoc.services.onlineOrdering;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.aggregtor.*;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.Discount;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminOrder;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminToken;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Item;
import com.sun.supplierpoc.models.aggregtor.foodics.Meta;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.configurations.AggregatorConfiguration;
import com.sun.supplierpoc.models.configurations.SimphonyAccount;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.applications.ProductRepository;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import com.sun.supplierpoc.services.restTemplate.TalabatAdminWebService;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Modifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregatorIntegratorService {

    @Autowired
    private FoodicsWebServices foodicsWebServices;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private TalabatAdminWebService talabatAdminWebService;

    /*
    * synchronize orders between talabat and foodics
    * */
    public Response sendTalabatOrdersToFoodics(Account account) {

        Response response = new Response();
        AggregatorOrder order;

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        for (BranchMapping branch : aggregatorConfiguration.getBranchMappings()) {
                // Check if branch configured or not
                if(branch.getPassword().isEmpty() || branch.getUsername().isEmpty())
                    continue;

                // Login to talabat resturant branch
                TalabatAdminToken talabatAdminToken = talabatAdminWebService.talabatLoginRequest(account, branch);

                // Get all branch orders
                TalabatAdminOrder[] branchOrders = talabatAdminWebService.getAllOrderDetails(talabatAdminToken);

                if (branchOrders != null && branchOrders.length > 0) {
                    try {
                        for (TalabatAdminOrder talabatAdminOrder : branchOrders) {
                            order = new AggregatorOrder();

                            // Prepare foodics order
                            FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatAdminOrder, branch, generalSettings);

                            if(foodicsOrder != null){
                                foodicsOrder = foodicsWebServices.sendOrderToFoodics(foodicsOrder, generalSettings, foodicsAccountData);

                                if (foodicsOrder.isCallStatus()) {
                                    order.setOrderStatus(Constants.SUCCESS);
                                }
                                else {
                                    order.setOrderStatus(Constants.FAILED);
                                    order.setReason(foodicsOrder.getMessage());
                                }
                                order.setFoodicsOrder(foodicsOrder);
                            }

                            order.setCreationDate(new Date());
                            order.setTalabatAdminOrder(talabatAdminOrder);
                            order.setAggregatorName(AggregatorConstants.TALABAT);
                            order.setAccount(account);
                            orderRepo.save(order);
                        }

                        response.setMessage("Send Talabat Orders Successfully");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    response.setStatus(false);
                    response.setMessage("Login To Talabat Failed Due To, Please contact support team.");
                }
            }

        return response;
    }

    public Response fetchProducts(Account account) {

        Response response = new Response();

        try {
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            List<Aggregator> aggregators = generalSettings.getAggregators();
            AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();

            Product product = new Product();

            for (Aggregator aggregator : aggregators) {
                if (aggregator.getName().equals(AggregatorConstants.FOODICS)) {
                    FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();
                    product = foodicsWebServices.fetchProducts(generalSettings, foodicsAccountData);
                } else if (aggregator.getName().equals(AggregatorConstants.SIMPHONY)) {
                    SimphonyAccount simphonyAccount = aggregatorConfiguration.getSimphonyAccount();
                }
            }
            productRepository.save(product);
            response.setData(product);
        }catch (Exception e){
            response.setMessage("Can't sync products due tue error: " + e.getMessage());
            response.setStatus(false);
        }
        return response;
    }

    private FoodicsOrder parseOrderParametersToFoodics(TalabatAdminOrder adminOrder,
                                                       BranchMapping branchMapping,
                                                       GeneralSettings generalSettings) {

        FoodicsOrder foodicsOrder = new FoodicsOrder();
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getAggregatorConfiguration();

        try {
            // Order Type 3 ==> Delivery
            foodicsOrder.setType(3);
            foodicsOrder.setCheck_number(adminOrder.externalId);

            Meta meta = new Meta();
            meta.setExternalNumber(adminOrder.shortCode);
            foodicsOrder.setMeta(meta);

            // Customer Details
            if(adminOrder.getCustomer() != null){
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(adminOrder.getCustomer().getPhone(), "");

                foodicsOrder.setCustomerName(adminOrder.getCustomer().getFirstName() + " " + adminOrder.getCustomer().getLastName());
                foodicsOrder.setCustomerDialCode(String.valueOf(numberProto.getCountryCode()));
                foodicsOrder.setCustomerPhone(String.valueOf(numberProto.getNationalNumber()));
            }


            if(adminOrder.getCustomer() != null){
                foodicsOrder.setCustomerAddressName("Home"); // "Home/Work"
                foodicsOrder.setCustomerAddressDescription(adminOrder.getAddress().street
                        + "-" + adminOrder.getAddress().city
                        + "-" + adminOrder.getAddress().area);
                foodicsOrder.setCustomerAddressLatitude(String.valueOf(adminOrder.getAddress().latitude));
                foodicsOrder.setCustomerAddressLongitude(String.valueOf(adminOrder.getAddress().longitude));
            }

            // Branch information
            foodicsOrder.setBranchId(branchMapping.getFoodIcsBranchId());

            // Order products
            ProductsMapping productsMapping;
            ModifierMapping modifierMapping;

            FoodicsProductObject foodicsProductObject;
            List<FoodicsProductObject> foodicsProductObjects = new ArrayList<>();

            ArrayList<Option> options = new ArrayList<>();

            for (Item item : adminOrder.getItems()) {
                foodicsProductObject = new FoodicsProductObject();

                // Item information
                productsMapping = aggregatorConfiguration.getProductsMappings().stream().
                        filter(tempProduct -> tempProduct.getTalabatProductId().equals(item.getProductId()))
                        .collect(Collectors.toList()).stream().findFirst().orElse(null);

                if (productsMapping != null) {
                    // Normal Product
                    if(productsMapping.getModifiers().size() == 0){
                        if (productsMapping.getFoodIcsProductId() == null || productsMapping.getFoodIcsProductId().equals(""))
                            continue; // Skip this product
                        foodicsProductObject.setProduct_id(productsMapping.getFoodIcsProductId());
                        foodicsProductObject.setUnit_price(item.getPrice());
                    }
                    // Product combination (Product + Extra) or (Product + Extra + Extra)
                    else {
                        foodicsProductObject.setUnit_price(item.getPrice());

                        ArrayList<ModifierMapping> firstFilteredModifiers = new ArrayList<>();
                        ArrayList<ModifierMapping> newModifiers = new ArrayList<>();

                        // Filter product modifiers based on first modifier
                        for (ModifierMapping modifierMap : productsMapping.getModifiers()) {
                            // check if first id exists in item's extras (modifiers)
                            Modifier itemMod = item.modifiers.stream().filter(tempModifier -> tempModifier.getProductId().equals(modifierMap.getTalabatProductId()))
                                    .collect(Collectors.toList()).stream().findFirst().orElse(null);
                            if(itemMod != null){
                                firstFilteredModifiers.add(modifierMap);

                                foodicsProductObject.setUnit_price(foodicsProductObject.getUnit_price() + itemMod.getPrice());
                                item.modifiers.remove(itemMod);
                            }
                        }

                        // Filter product modifiers based on second modifier, if the fist modifier require second one
                        for (ModifierMapping modifierMap : firstFilteredModifiers) {
                            // check if second id exists in item's extras (modifiers)
                            if (modifierMap.getTalabatSecProductId() == null || modifierMap.getTalabatSecProductId().equals("")) {
                                newModifiers.add(modifierMap); break;
                            } else {
                                Modifier itemMod = item.modifiers.stream().filter(tempModifier -> tempModifier.getProductId().equals(modifierMap.getTalabatSecProductId()))
                                        .collect(Collectors.toList()).stream().findFirst().orElse(null);
                                if (itemMod != null) {
                                    newModifiers.add(modifierMap);

                                    foodicsProductObject.setUnit_price(foodicsProductObject.getUnit_price() + itemMod.getPrice());
                                    item.modifiers.remove(itemMod);
                                }
                            }
                        }

                        if(newModifiers.size() > 0 ){
                            foodicsProductObject.setProduct_id(newModifiers.get(0).getFoodicsProductId());
                        }else {
                            continue; // Skip this product
                        }
                    }

                    //if it's a combo offer product
                    if(productsMapping.isCombo()){
                        modifierMapping = generalSettings.getTalabatConfiguration().getModifierMappings().stream().
                                filter(tempProduct -> tempProduct.getName().equals("Combo Talabat"))
                                .collect(Collectors.toList()).stream().findFirst().orElse(null);

                        if (modifierMapping != null) {
                            Option option = new Option();
                            option.setModifier_option_id(modifierMapping.getFoodicsProductId());
                            option.setQuantity(1);
                            option.setUnit_price(0.0);
                            options.add(option);
                        }
                    }
                }
                else {
                    continue; // Skip this product
                }

                foodicsProductObject.setQuantity(item.getAmount());
                if(item.getComment() != null)
                    foodicsProductObject.setKitchen_notes(item.getComment());

                // Options
                Option option;
                Option secondOption;
                ArrayList<Option> extraProductOptions = new ArrayList<>();

                if(item.getModifiers() != null && !productsMapping.isCombo()){
                    for (Modifier modifier: item.getModifiers()) {
                        option = new Option();

                        // Extra Info
                        modifierMapping = generalSettings.getTalabatConfiguration().getModifierMappings().stream().
                                filter(tempProduct -> tempProduct.getName().equals(modifier.getName()))
                                .collect(Collectors.toList()).stream().findFirst().orElse(null);

                        if (modifierMapping != null) {
                            ///check on product flag
                            if(modifierMapping.isProduct()){
                                extraProductOptions = new ArrayList<>();

                                FoodicsProductObject extraProductObject = new FoodicsProductObject();
                                extraProductObject.setQuantity(modifier.getAmount());
                                extraProductObject.setProduct_id(modifierMapping.getFoodicsProductId());
                                extraProductObject.setUnit_price(modifier.getPrice());

                                // Add options
                                if(!modifierMapping.getSecondFoodicsProductId().equals("")){
                                    secondOption = new Option();
                                    secondOption.setModifier_option_id(modifierMapping.getSecondFoodicsProductId());
                                    secondOption.setQuantity(1);
                                    secondOption.setUnit_price(0);
                                    extraProductOptions.add(secondOption);
                                }

                                extraProductObject.setOptions(extraProductOptions);
                                foodicsProductObjects.add(extraProductObject);
                                continue;
                            }else{
                                option.setModifier_option_id(modifierMapping.getFoodicsProductId());
                            }
                        } else
                            continue; // Skip this product's modifier

                        option.setQuantity(modifier.getAmount());
                        option.setUnit_price(modifier.getPrice());

                        options.add(option);

                        // Tax information
/*                        Tax tax;
                        ArrayList<Tax> taxes = new ArrayList<>();

                        tax = new Tax();

                        tax.setId("9598c557-72ed-4076-8f01-181bb47a65bc"); // To be added
                        tax.setAmount(0);
                        tax.setRate(0); // To be added
                        taxes.add(tax);

                        option.setTaxes(taxes);*/

                    }
                }


                foodicsProductObject.setOptions(options);

                foodicsProductObjects.add(foodicsProductObject);
            }
            foodicsOrder.setProducts(foodicsProductObjects);

            // Discount Information
            double totalDiscount = 0.0;
            if(adminOrder.discounts != null){
                for (Discount discount : adminOrder.discounts) {
                    if(discount.value == 0)
                        continue;
                    totalDiscount += discount.value;
                }
            }

            foodicsOrder.setDiscount_amount(totalDiscount * -1);
            foodicsOrder.setDiscount_type(1); // Open, takes order creator value and is always

            // Tax information
            // Foodics system will auto apply the taxes based on the account setting for taxes.
            Tax tax = new Tax();
            ArrayList<Tax> taxes = new ArrayList<>();
            for (com.sun.supplierpoc.models.aggregtor.TalabatRest.Tax orderTax : adminOrder.taxes) {
                if(orderTax.value == 0)
                    continue;

                tax = new Tax();

                tax.setId(""); // To be added
                tax.setAmount(orderTax.value);
                tax.setRate(0); // To be added
                taxes.add(tax);
            }

            // Charges
            List<Charge> charges = new ArrayList<>();
//            Charge charge = new Charge();
//
//            charge.setCharge_id(""); // To be added
//            charge.setAmount(0);
//            charge.setTaxes(taxes);
//            charges.add(charge);

            foodicsOrder.setCharges(charges);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodicsOrder;
    }

    public void dropOrdersCollection() {
        this.orderRepo.deleteAll();
    }

}

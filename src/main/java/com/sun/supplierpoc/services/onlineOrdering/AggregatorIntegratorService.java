package com.sun.supplierpoc.services.onlineOrdering;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.aggregtor.Aggregator;
import com.sun.supplierpoc.models.aggregtor.AggregatorConstants;
import com.sun.supplierpoc.models.aggregtor.BranchMapping;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Fee;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminOrder;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminToken;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.aggregtor.ProductsMapping;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Item;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.TalabatAggregatorOrder;
import com.sun.supplierpoc.models.aggregtor.login.Token;
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
import org.springframework.stereotype.Service;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Modifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    * Syncronize orders between talabat and foodics
    * */
    public Response sendTalabatOrdersToFoodics(Account account) {

        Response response = new Response();
        AggregatorOrder order = new AggregatorOrder();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        Token talabatPortalToken = talabatRestService.talabatLoginRequest(account);

        if (talabatPortalToken != null && talabatPortalToken.isStatus()) {

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

                            order.setTalabatAdminOrder(talabatAdminOrder);
                            order.setAggregatorName(AggregatorConstants.TALABAT);
                            orderRepo.save(order);

                            // To be removed after testing
                            // break;
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

        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + talabatPortalToken.getMessage());
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

    public LinkedHashMap updateFoodicsProduct(Account account, FoodicsProduct foodicsProduct) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        Product product = new Product();

        try {
//            FoodicsProduct product = foodicsProductRepo.findById(foodicsProduct.getId()).orElse(null);

            if (product != null) {

//                foodicsProductRepo.save(foodicsProduct);

                response.put("message", "Product information was successfully updated.");
                response.put("status", "success");
                response.put("data", foodicsProduct);

            } else {

                response.put("message", "Product Not Found.");
                response.put("status", "failed");

            }
        } catch (Exception e) {
            response.put("message", "Can't save foodics product.");
            response.put("status", "failed");
        }

        return response;
    }

    private FoodicsOrder parseOrderParametersToFoodics(TalabatAdminOrder adminOrder,
                                                       BranchMapping branchMapping,
                                                       GeneralSettings generalSettings) {

        FoodicsOrder foodicsOrder = new FoodicsOrder();

        try {
            // Order Type 3 ==> Delivery
            foodicsOrder.setType(3);
            foodicsOrder.setCheck_number(adminOrder.externalId);
            foodicsOrder.setKitchen_notes(""); // To be added

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
            ProductsMapping productsMapping = new ProductsMapping();

            FoodicsProductObject foodicsProductObject;
            List<FoodicsProductObject> foodicsProductObjects = new ArrayList<>();

            for (Item item : adminOrder.getItems()) {
                foodicsProductObject = new FoodicsProductObject();

                // Item information
                productsMapping = generalSettings.getTalabatConfiguration().getProductsMappings().stream().
                        filter(tempProduct -> tempProduct.getTalabatProductId() == item.getId())
                        .collect(Collectors.toList()).stream().findFirst().orElse(null);

                if (productsMapping != null)
                    foodicsProductObject.setProduct_id(productsMapping.getFoodIcsProductId());
                else
                    foodicsProductObject.setProduct_id("9597379c-a45c-4c9c-963b-27d383e34085");

                foodicsProductObject.setQuantity(item.getAmount());
                foodicsProductObject.setUnit_price(item.getPrice());

                // Options
                Option option;
                ArrayList<Option> options = new ArrayList<>();
                if(item.getModifiers() != null){
                    for (Modifier modifier: item.getModifiers()) {
                        option = new Option();

                        option.setModifier_option_id("9598daea-07ad-418e-b769-8387e678c998"); // To be added
                        option.setQuantity(modifier.getAmount());
                        option.setUnit_price(modifier.getPrice());

                        // Tax information
                        Tax tax = new Tax();
                        ArrayList<Tax> taxes = new ArrayList<>();

                        tax = new Tax();

                        tax.setId("9598c557-72ed-4076-8f01-181bb47a65bc"); // To be added
                        tax.setAmount(0);
                        tax.setRate(0); // To be added
                        taxes.add(tax);

                        option.setTaxes(taxes);

                        options.add(option);
                    }
                }
                foodicsProductObject.setOptions(options);

                foodicsProductObjects.add(foodicsProductObject);
            }
            foodicsOrder.setProducts(foodicsProductObjects);

            // Tax information
            Tax tax = new Tax();
            ArrayList<Tax> taxes = new ArrayList<>();
            for (Fee fee : adminOrder.fees) {
                if(fee.value == 0)
                    continue;

                tax = new Tax();

                tax.setId(""); // To be added
                tax.setAmount(fee.value);
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

    public LinkedHashMap updateFoodicsOrder(Account account, FoodicsOrder tempFoodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {

            AggregatorOrder order = orderRepo.findByFoodicsOrderId(tempFoodicsOrder.getId()).orElse(null);

            FoodicsOrder foodicsOrder = order.getFoodicsOrder();

            String delivery_status = "";
            String status = "";

            if (foodicsOrder != null) {


                switch (tempFoodicsOrder.getDelivery_status()) {
                    case 1:
                        delivery_status = "sent to kitchen";
                        break;
                    case 2:
                        delivery_status = "ready";
                        break;
                    case 3:
                        delivery_status = "assigned";
                        break;
                    case 4:
                        delivery_status = "en route";
                        break;
                    case 5:
                        delivery_status = "delivered";
                        break;
                    case 6:
                        delivery_status = "closed";
                        break;
                }

                switch (tempFoodicsOrder.getStatus()) {
                    case 1:
                        status = "Pending";
                        break;
                    case 2:
                        status = "Active";
                        break;
                    case 3:
                        status = "Declined";
                        break;
                    case 4:
                        status = "Closed";
                        break;
                    case 5:
                        status = "Returned";
                        break;
                    case 6:
                        status = "Void";
                        break;
                }

                order.setFoodicsOrder(foodicsOrder);

                orderRepo.save(order);

                response.put("message", "Order information was successfully updated.");
                response.put("status", "success");
                response.put("orderStatus", status);
                response.put("deliveryStatus", delivery_status);
            } else {
                response.put("message", "Order Not Found.");
                response.put("status", false);
            }

        } catch (Exception e) {
            response.put("message", "Can't save foodics order.");
            response.put("status", false);
        }
        return response;
    }

}

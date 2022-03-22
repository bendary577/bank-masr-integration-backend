package com.sun.supplierpoc.services.onlineOrdering;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.aggregtor.Aggregator;
import com.sun.supplierpoc.models.aggregtor.BranchMapping;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.TalabatAdminOrder;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.aggregtor.ProductsMapping;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.Item;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.TalabatOrder;
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

    public Response sendReceivedOrders(Account account) {

        Response response = new Response();
        com.sun.supplierpoc.models.Order order = new com.sun.supplierpoc.models.Order();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        AggregatorConfiguration aggregatorConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();

        Token talabatToken = talabatRestService.talabatLoginRequest(account);

        if (talabatToken != null && talabatToken.isStatus()) {
            ArrayList<String> branches = new ArrayList<>();
            for (BranchMapping branch : aggregatorConfiguration.getBranchMappings()) {
                branches.add(branch.getTalabatBranchId());
            }

            TalabatOrder talabatOrder = talabatRestService.getOrders(talabatToken, branches);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                List<RestOrder> receivedOrders = talabatOrder.getOrders().stream()
                        .filter(restOrder -> restOrder.getOrder_status().equalsIgnoreCase("Unknown") ||
                                restOrder.getOrder_status().equalsIgnoreCase("ACCEPTED") ||
                                        restOrder.getOrder_status().equalsIgnoreCase("DELAYED"))
                        .collect(Collectors.toList());

                try {
                    List<TalabatOrder> talabatOrderList = new ArrayList<>();
                    TalabatOrder talabatOrderDetails = new TalabatOrder();


                    for (RestOrder restOrder : receivedOrders) {
                        // Get Items Only
                        talabatOrderDetails = talabatRestService.getOrderById(restOrder, talabatToken);

                        // Login to talabat resturant branch
                        BranchMapping branchMapping = generalSettings.getTalabatConfiguration().getBranchMappings().stream().
                                filter(branch -> branch.getTalabatBranchId().equals(restOrder.getGlobal_vendor_code()))
                                .collect(Collectors.toList()).stream().findFirst().orElse(new BranchMapping());
                        if(branchMapping == null)
                           continue;

                        com.sun.supplierpoc.models.aggregtor.branchAdmin.Token talabatAdminToken =
                                talabatAdminWebService.talabatLoginRequest(account, branchMapping);
                        // Get Customer Details
                        TalabatAdminOrder talabatAdminOrder = talabatAdminWebService.getOrderDetails(talabatAdminToken, restOrder);

                        if(talabatAdminOrder.isStatus()){
                            // Prepare foodics order
                            FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatOrderDetails, talabatAdminOrder, generalSettings);

                            if(foodicsOrder != null){
                                foodicsOrder = foodicsWebServices.sendOrderToFoodics(foodicsOrder, generalSettings, foodicsAccountData);
                                talabatOrderDetails.setOrders(List.of(restOrder));

                                if (foodicsOrder.isCallStatus()) {
                                    order.setOrderStatus(Constants.SUCCESS);
                                }
                                else {
                                    order.setOrderStatus(Constants.FAILED);
                                    order.setReason(foodicsOrder.getMessage());
                                }

                                talabatOrderList.add(talabatOrderDetails);
                                order.setFoodicsOrder(foodicsOrder);
                            }
                        }else{
                            order.setOrderStatus(Constants.FAILED);
                            order.setReason(talabatAdminOrder.getMessage());
                        }
                        orderRepo.save(order);

                        // To be removed after testing
//                        break;
                    }

                    if (talabatOrderList.size() > 0) {
                        response.setMessage("Send Talabat Orders Successfully");
                        response.setData(talabatOrderList.get(0));
                    } else {
                        response.setMessage("Send Talabat Orders Successfully");
                        response.setData(new TalabatOrder());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + talabatToken.getMessage());
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
                if (aggregator.getName().equals(Constants.FOODICS)) {
                    FoodicsAccountData foodicsAccountData = aggregatorConfiguration.getFoodicsAccount();
                    product = foodicsWebServices.fetchProducts(generalSettings, foodicsAccountData);
                } else if (aggregator.getName().equals(Constants.SIMPHONY)) {
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

    private FoodicsOrder parseOrderParametersToFoodics(TalabatOrder talabatOrder, TalabatAdminOrder adminOrder,
                                                       GeneralSettings generalSettings) {

        com.sun.supplierpoc.models.aggregtor.TalabatRest.Order parsedOrder = talabatOrder.getOrder();
        FoodicsOrder foodicsOrder = new FoodicsOrder();

        try {
            // Resturant Branch
            BranchMapping branchMapping = generalSettings.getTalabatConfiguration().getBranchMappings().stream().
                    filter(branch -> branch.getTalabatBranchId().equals(parsedOrder.getGlobalVendorCode()))
                    .collect(Collectors.toList()).stream().findFirst().orElse(new BranchMapping());
            if(branchMapping != null)
                foodicsOrder.setBranchId(branchMapping.getFoodIcsBranchId());
            else
                return null;

            // Cutomer Details
            foodicsOrder.setGuests(1);

            // Order Stataus
            foodicsOrder.setType(3);
            if(adminOrder.getCustomer() != null){
                foodicsOrder.setCustomerAddressName(adminOrder.getCustomer().getFirstName());
                foodicsOrder.setCustomerName(adminOrder.getCustomer().getFirstName());
                foodicsOrder.setCustomerDialCode("966");
                foodicsOrder.setCustomerPhone(adminOrder.getCustomer().getPhone().replace("+", ""));
            }

            if(adminOrder.getCustomer() != null){
                foodicsOrder.setCustomerAddressName("Work");
                foodicsOrder.setCustomerAddressDescription(adminOrder.getAddress().street
                        + "-" + adminOrder.getAddress().city
                        + "-" + adminOrder.getAddress().area);
                foodicsOrder.setCustomerAddressLatitude(String.valueOf(adminOrder.getAddress().latitude));
                foodicsOrder.setCustomerAddressLongitude(String.valueOf(adminOrder.getAddress().longitude));
            }

            // Order Items
            ProductsMapping productsMapping = new ProductsMapping();
            List<FoodicsProductObject> foodicsProductObjects = new ArrayList<>();
            FoodicsProductObject foodicsProductObject = new FoodicsProductObject();

            for (Item item : parsedOrder.getItems()) {
                productsMapping = generalSettings.getTalabatConfiguration().getProductsMappings().stream().
                        filter(tempProduct -> tempProduct.getTalabatProductId() == item.getId())
                        .collect(Collectors.toList()).stream().findFirst().orElse(null);

                foodicsProductObject = new FoodicsProductObject();

                Option option;
                List<Option> options;
                option = new Option();
                options = new ArrayList<>();
                options.add(option);
                foodicsProductObject.setOptions(options);

                if (productsMapping != null) {
                    foodicsProductObject.setProductId(productsMapping.getFoodIcsProductId());
                } else {
                    foodicsProductObject.setProductId("9590e5a9-f20a-4f8f-beb3-14710c688a89");
                }

                foodicsProductObject.setQuantity(item.getQuantity());
                foodicsProductObject.setUnitPrice(Double.parseDouble(item.getUnitPrice()));
                foodicsProductObjects.add(foodicsProductObject);
            }

            foodicsOrder.setProducts(foodicsProductObjects);

            // Order
//        foodicsOrder.setSubtotalPrice(parsedOrder.getPayment().getTotal());
//        foodicsOrder.setRoundingAmount(0.14);
//        foodicsOrder.setDiscountAmount(parsedOrder.getPayment().getDiscount());
//        foodicsOrder.setTotalPrice(parsedOrder.getPayment().getTotal() - parsedOrder.getPayment().getDiscount());

//        double discountRate = (parsedOrder.getPayment().getDiscount() / parsedOrder.getPayment().getTotal()) * 100;
//
//        DiscountMapping discountMapping = generalSettings.getTalabatConfiguration().getDiscountMappings().stream().
//                filter(discount -> discount.getDiscountRate() == discountRate)
//                .collect(Collectors.toList()).stream().findFirst().orElse(new DiscountMapping());
//
//        foodicsOrder.setDiscountId(discountMapping.getDiscountId().toString());
//
//        foodicsOrder.setKitchenNotes(talabatOrder.getOrderStatuses().get(0).getSentToTransmissionDetails().getComment());
//        foodicsOrder.setDueAt(parsedOrder.getOrderTimestamp());
//        foodicsOrder.setTableId("1");
//        foodicsOrder.setCustomerNotes("");
//        foodicsOrder.setDriverId(parsedOrder.getDelivery().toString());

//            CustomerMapping customerMapping = generalSettings.getTalabatConfiguration().
//                    getCustomerMappings().get(0);
//
//            foodicsOrder.setCustomerId("9598f397-2a95-4314-b239-213327b6b422");
//
//            AddressMapping addressMapping = generalSettings.getTalabatConfiguration().
//                    getAddressMappings().get(0);
//
//            foodicsOrder.setCustomerAddressId("");

//        Meta meta = new Meta();
//        meta.setExternalNumber("120153");
//        foodicsOrder.setMeta(meta);
//
//        List<Charge> charges = new ArrayList<>();
//        Charge charge = new Charge();
//        charge.setAmount(0);
//        charge.setTaxExclusiveAmount(0);
//
//        List<Tax> taxes = new ArrayList<>();
//        Tax tax = new Tax();
//        tax.setAmount(Double.parseDouble("0"));
//        tax.setRate(Integer.parseInt("0"));
//        taxes.add(tax);
//
//        charge.setTaxes(taxes);
//        charges.add(charge);
//
//        foodicsOrder.setCharges(charges);

//        foodicsOrder.setProducts(products);
//        foodicsOrder.setCombos(new ArrayList<Combo>());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodicsOrder;
    }

    public LinkedHashMap updateFoodicsOrder(Account account, FoodicsOrder tempFoodicsOrder) {

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();

        try {

            Order order = orderRepo.findByFoodicsOrderId(tempFoodicsOrder.getId()).orElse(null);

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

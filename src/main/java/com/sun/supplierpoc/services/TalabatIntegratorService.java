package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.TalabatConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.BranchMapping;
import com.sun.supplierpoc.models.talabat.FoodicsProduct;
import com.sun.supplierpoc.models.talabat.ProductsMapping;
import com.sun.supplierpoc.models.talabat.TalabatRest.Item;
import com.sun.supplierpoc.models.talabat.TalabatRest.Order;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
import com.sun.supplierpoc.models.talabat.foodics.*;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.repositories.applications.FoodicsOrderRepo;
import com.sun.supplierpoc.repositories.applications.FoodicsProductRepo;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalabatIntegratorService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private FoodicsProductRepo foodicsProductRepo;

    @Autowired
    private FoodicsOrderRepo foodicsOrderRepo;

    public Response syncFoodicsOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                response.setMessage("Sync Talabat Orders Successfully");
                response.setData(talabatOrder);

            } else {
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response syncFoodicsBranchOrders(Account account, String branch) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token, branch);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                response.setMessage("Sync Talabat Orders Successfully");
                response.setData(talabatOrder);

            } else {
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response sendReceivedOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token);

            if (talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null) {

                List<RestOrder> receivedOrders = talabatOrder.getOrders().stream()
                        .filter(restOrder -> restOrder.getOrder_status().equals("ACCEPTED"))
                        .collect(Collectors.toList());

//                List<RestOrder> receivedOrders = List.of(talabatOrder.getOrders().get(0));
                try {
                    List<TalabatOrder> talabatOrderList = new ArrayList<>();
                    TalabatOrder talabatOrderDetails = new TalabatOrder();

                    FoodicsLoginBody foodicsLoginBody = new FoodicsLoginBody();
                    //talabatRestService.LoginToFoodics();

                    for (RestOrder restOrder : receivedOrders) {
                        talabatOrderDetails = talabatRestService.getOrderById(restOrder, token);

                        FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatOrderDetails, generalSettings);

                        if (foodicsOrder != null) {
                            talabatOrderList.add(talabatOrderDetails);
                        } else {

                        }
                        foodicsOrder = talabatRestService.sendOrderToFoodics(foodicsOrder, foodicsLoginBody, generalSettings, foodicsAccount);
                        talabatOrderDetails.setOrders(List.of(restOrder));
                        talabatOrderList.add(talabatOrderDetails);
                    }

                    if (talabatOrderList.size() > 0) {
                        orderRepo.saveAll(talabatOrderList);
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
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    private FoodicsOrder parseOrderParametersToFoodics(TalabatOrder talabatOrder, GeneralSettings generalSettings) {

        Order parsedOrder = talabatOrder.getOrder();
        FoodicsOrder foodicsOrder = new FoodicsOrder();

        try {


//        foodicsOrder.setGuests(1);
            foodicsOrder.setType(1);

//        BranchMapping branchMapping = generalSettings.getTalabatConfiguration().getBranchMappings().stream().
//                filter(branch -> branch.getTalabatBranchId() == parsedOrder.getGlobalVendorCode())
//                .collect(Collectors.toList()).stream().findFirst().orElse(new BranchMapping());

            BranchMapping branchMapping = generalSettings.getTalabatConfiguration().getBranchMappings().get(0);

            foodicsOrder.setBranchId(branchMapping.getFoodIcsBranchId());

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
//        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
//        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
//        foodicsOrder.setCustomerId(parsedOrder.getDelivery().getAddressText());

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

            ProductsMapping productsMapping = new ProductsMapping();
            List<FoodicsProduct> products = new ArrayList<>();
            FoodicsProduct product = new FoodicsProduct();

            for (Item item : parsedOrder.getItems()) {

//            productsMapping = generalSettings.getTalabatConfiguration().getProductsMappings().stream().
//                    filter(tempProduct -> tempProduct.getTalabatProductId() == item.getId())
//                    .collect(Collectors.toList()).stream().findFirst().orElse(null);

                productsMapping = generalSettings.getTalabatConfiguration().getProductsMappings().get(0);

                if (productsMapping != null) {

                    product = new FoodicsProduct();

//                Option option;
//                List<Option> options;
//                option = new Option();
//                options = new ArrayList<>();
//                options.add(option);
//                product.setOptions(options);

                    product.setProductId(productsMapping.getFoodIcsProductId());
                    product.setQuantity(item.getQuantity());
                    product.setUnitPrice(Double.parseDouble(item.getUnitPrice()));

                    products.add(product);

                } else {
                    return null;
                }
            }

            foodicsOrder.setProducts(products);


//        foodicsOrder.setProducts(products);
//        foodicsOrder.setCombos(new ArrayList<Combo>());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return foodicsOrder;
    }

    public Response getOrderDtails(Account account, RestOrder order) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        Token token = talabatRestService.talabatLoginRequest(account);

        if (token != null && token.isStatus()) {

            TalabatOrder talabatOrderDetails = talabatRestService.getOrderById(order, token);
            response.setData(talabatOrderDetails);
            response.setStatus(true);
            response.setMessage("Success");
        } else {
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    public Response fetchProducts(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
        TalabatConfiguration talabatConfiguration = generalSettings.getTalabatConfiguration();
        FoodicsAccount foodicsAccount = talabatConfiguration.getFoodicsAccount();

        response = talabatRestService.fetchProducts(generalSettings, foodicsAccount);

        ArrayList<Product> foodicsProducts = response.getFoodicsProducts();

        try {
//            foodicsProductRepo.saveAll(foodicsProducts);
        } catch (Exception e) {
            response.setMessage("Can't save foodics product.");
            response.setStatus(false);
        }

        return response;
    }

    public Product updateFoodicsProdu(Account account, Product foodicsProduct) {

        Response response = new Response();

        try {
//            foodicsProductRepo.save(foodicsProduct);
        } catch (Exception e) {
            response.setMessage("Can't save foodics product.");
            response.setStatus(false);
        }

        return foodicsProduct;
    }

    public FoodicsOrder updateFoodicsOrder(Account account, FoodicsOrder foodicsOrder) {

        HashMap<String, Object> response = new HashMap<>();

        try {

            if (foodicsOrderRepo.existsById(foodicsOrder.getId())) {

                foodicsOrderRepo.save(foodicsOrder);
                String delivery_status = "";
                switch (foodicsOrder.getDelivery_status()) {
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

                String status = "";
                switch (foodicsOrder.getStatus()) {
                    case 1:
                        delivery_status = "Pending";
                        break;
                    case 2:
                        delivery_status = "Active";
                        break;
                    case 3:
                        delivery_status = "Declined";
                        break;
                    case 4:
                        delivery_status = "Closed";
                        break;
                    case 5:
                        delivery_status = "Returned";
                        break;
                    case 6:
                        delivery_status = "Void";
                        break;
                }

            } else {
                response.put("message", "Order dosen't exist.");
                response.put("status", false);
            }

        } catch (Exception e) {
            response.put("message", "Can't save foodics order.");
            response.put("status", false);
        }
        return foodicsOrder;
    }
}

package com.sun.supplierpoc.services.onlineOrdering;

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
import com.sun.supplierpoc.models.talabat.foodics.FoodicsLoginBody;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
import com.sun.supplierpoc.services.restTemplate.FoodicsWebServices;
import com.sun.supplierpoc.services.restTemplate.TalabatRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TalabatToFoodicsService {

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    @Autowired
    private FoodicsWebServices foodicsWebServices;

    @Autowired
    private TalabatRestService talabatRestService;

    @Autowired
    private OrderRepo orderRepo;


    public Response sendReceivedOrders(Account account) {

        Response response = new Response();
        com.sun.supplierpoc.models.Order order = new com.sun.supplierpoc.models.Order();

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
                    List<com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder> talabatOrderList = new ArrayList<>();
                    com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder talabatOrderDetails = new TalabatOrder();

                    FoodicsLoginBody foodicsLoginBody = new FoodicsLoginBody();
                    //talabatRestService.LoginToFoodics();

                    for (RestOrder restOrder : receivedOrders) {
                        talabatOrderDetails = talabatRestService.getOrderById(restOrder, token);

                        FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatOrderDetails, generalSettings);

                        if (foodicsOrder != null) {
                            talabatOrderList.add(talabatOrderDetails);
                        } else {

                        }
                        foodicsOrder = foodicsWebServices.sendOrderToFoodics(foodicsOrder, foodicsLoginBody, generalSettings, foodicsAccount);
                        talabatOrderDetails.setOrders(List.of(restOrder));

                        if (foodicsOrder.isCallStatus()) {
                            talabatOrderList.add(talabatOrderDetails);
                            order.setFoodicsOrder(foodicsOrder);
                            orderRepo.save(order);
                        }

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
            foodicsOrder.setType(3);
//            foodicsOrder

//        BranchMapping branchMapping = generalSettings.getTalabatConfiguration().getBranchMappings().stream().
//                filter(branch -> branch.getTalabatBranchId() == parsedOrder.getGlobalVendorCode())
//                .collect(Collectors.toList()).stream().findFirst().orElse(new BranchMapping());

            BranchMapping branchMapping = generalSettings.getTalabatConfiguration().
                    getBranchMappings().get(0);

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

}

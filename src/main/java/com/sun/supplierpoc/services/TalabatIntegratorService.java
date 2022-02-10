package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.talabat.BranchMapping;
import com.sun.supplierpoc.models.talabat.DiscountMapping;
import com.sun.supplierpoc.models.talabat.TalabatRest.*;
import com.sun.supplierpoc.models.talabat.foodics.*;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.OrderRepo;
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

    public Response sendReceivedOrders(Account account) {

        Response response = new Response();

        GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

        Token token = talabatRestService.talabatLoginRequest(account);

        if(token != null && token.isStatus() ) {

            TalabatOrder talabatOrder = talabatRestService.getOrders(token);

            if(talabatOrder != null && talabatOrder.getStatus() && talabatOrder.getOrders() != null ) {

                response.setMessage("Sync Talabat Orders Successfully");
                response.setData(talabatOrder);

                List<RestOrder> receivedOrders = talabatOrder.getOrders().stream()
                        .filter(restOrder -> restOrder.getOrder_status().equals("CANCELLED"))
                        .collect(Collectors.toList());

                try {
                    List<TalabatOrder> talabatOrderList = new ArrayList<>();
                    TalabatOrder talabatOrderDetails = new TalabatOrder();

                    FoodicsLoginBody foodicsLoginBody = talabatRestService.LoginToFoodics();

                    for (RestOrder restOrder : receivedOrders) {
                        talabatOrderDetails = talabatRestService.getOrderById(restOrder, token);

                        FoodicsOrder foodicsOrder = parseOrderParametersToFoodics(talabatOrderDetails, generalSettings);

                        foodicsOrder = talabatRestService.sendOrderToFoodics(foodicsOrder, foodicsLoginBody);

                        if (foodicsOrder != null) {
//                        talabatOrderList.add(talabatOrderDetails);
                        }

                        talabatOrderDetails.setOrders(List.of(restOrder));

                        talabatOrderList.add(talabatOrderDetails);
                    }

                    if (talabatOrderList.size() > 0) {
                        orderRepo.saveAll(talabatOrderList);
                    }
                }catch(Exception e) {

                }
            }else{
                response.setStatus(false);
                response.setMessage("Login To Talabat Failed Due To : " + talabatOrder.getMessage());
            }
        }else{
            response.setStatus(false);
            response.setMessage("Login To Talabat Failed Due To : " + token.getMessage());
        }

        return response;
    }

    private FoodicsOrder parseOrderParametersToFoodics(TalabatOrder talabatOrder, GeneralSettings generalSettings) {

        Order parsedOrder = talabatOrder.getOrder();
        FoodicsOrder foodicsOrder = new FoodicsOrder();

        foodicsOrder.setGuests(1);
        foodicsOrder.setType(3);

        BranchMapping branchMapping = generalSettings.getBranchMappings().stream().
                filter(branch -> branch.getTalabatBranchId() == parsedOrder.getGlobalVendorCode())
                .collect(Collectors.toList()).stream().findFirst().orElse(new BranchMapping());

        foodicsOrder.setBranchId(branchMapping.getFoodIcsBranchId());
        foodicsOrder.setSubtotalPrice(parsedOrder.getPayment().getTotal());
        foodicsOrder.setRoundingAmount(0.14);
        foodicsOrder.setDiscountAmount(parsedOrder.getPayment().getDiscount());
        foodicsOrder.setTotalPrice(parsedOrder.getPayment().getTotal() - parsedOrder.getPayment().getDiscount());

        double discountRate = (parsedOrder.getPayment().getDiscount() / parsedOrder.getPayment().getTotal()) * 100;

        DiscountMapping discountMapping = generalSettings.getDiscountMappings().stream().
                filter(discount -> discount.getDiscountRate() == discountRate)
                .collect(Collectors.toList()).stream().findFirst().orElse(new DiscountMapping());

        foodicsOrder.setDiscountId(discountMapping.getDiscountId().toString());

        foodicsOrder.setKitchenNotes(talabatOrder.getOrderStatuses().get(0).getSentToTransmissionDetails().getComment());
        foodicsOrder.setDueAt(parsedOrder.getOrderTimestamp());
        foodicsOrder.setTableId("1");
        foodicsOrder.setCustomerNotes("");
        foodicsOrder.setDriverId(parsedOrder.getDelivery().toString());
        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
        foodicsOrder.setCustomerId(parsedOrder.getDelivery().getAddressText());

        Meta meta = new Meta();
        meta.setExternalNumber("120153");
        foodicsOrder.setMeta(meta);

        List<Charge> charges = new ArrayList<>();
        Charge charge = new Charge();
        charge.setAmount(0);
        charge.setTaxExclusiveAmount(0);

        List<Tax> taxes = new ArrayList<>();
        Tax tax = new Tax();
        tax.setAmount(Double.parseDouble("0"));
        tax.setRate(Integer.parseInt("0"));
        taxes.add(tax);

        charge.setTaxes(taxes);
        charges.add(charge);

        foodicsOrder.setCharges(charges);

        List<Product> products = new ArrayList<>();
        Product product = new Product();
        Option option;
        List<Option> options;
        for (Item item : parsedOrder.getItems()) {
            option = new Option();
            options = new ArrayList<>();
            options.add(option);
            product.setOptions(options);
            product.setProductId(item.getId());
            product.setQuantity(item.getQuantity());
        }

        foodicsOrder.setProducts(products);
        foodicsOrder.setCombos(new ArrayList<Combo>());


    return foodicsOrder;
    }
}

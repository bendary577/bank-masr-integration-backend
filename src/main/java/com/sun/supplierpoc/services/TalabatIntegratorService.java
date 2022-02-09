package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.talabat.BranchMapping;
import com.sun.supplierpoc.models.talabat.DiscountMapping;
import com.sun.supplierpoc.models.talabat.TalabatRest.Order;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
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

    public Response sendReceivedOrders(TalabatOrder talabatOrder){

        Response response = new Response();

        List<RestOrder> receivedOrders = talabatOrder.getOrders().stream()
                .filter(restOrder -> restOrder.getOrder_status().equals("ACCEPTED"))
                .collect(Collectors.toList());


        for(RestOrder restOrder : receivedOrders ){

            response = talabatRestService.getOrderById(restOrder, (Token) response.getData());

            FoodicsOrder order = parseOrderParametersToFoodics( (TalabatOrder) response.getData());

        }

        orderRepo.save(talabatOrder);

        return response;
    }

    private FoodicsOrder parseOrderParametersToFoodics(TalabatOrder talabatOrder, GeneralSettings generalSettings) {

        Order parsedOrder = talabatOrder.getOrder();
        FoodicsOrder foodicsOrder = new FoodicsOrder();

        foodicsOrder.setGuests(1);
        foodicsOrder.setType(3);

        BranchMapping branchMapping = generalSettings.getBranchMappings().stream().
                filter(branch -> branch.getTalabatBranchId() == parsedOrder.getGlobalVendorCode())
                .collect(Collectors.toList()).get(0);

        foodicsOrder.setBranchId(branchMapping.getFoodIcsBranchId());
        foodicsOrder.setSubtotalPrice(parsedOrder.getPayment().getTotal());
        foodicsOrder.setRoundingAmount(0.14);
        foodicsOrder.setDiscountAmount(parsedOrder.getPayment().getDiscount());
        foodicsOrder.setTotalPrice(parsedOrder.getPayment().getTotal() - parsedOrder.getPayment().getDiscount());

        double discountRate = ((parsedOrder.getPayment().getTotal() - parsedOrder.getPayment().getDiscount())
                 / parsedOrder.getPayment().getTotal()) * 100;

        Optional<DiscountMapping> discountMapping = generalSettings.getDiscountMappings().stream().
                filter(discount -> discount.getDiscountRate() == discountRate)
                .collect(Collectors.toList()).stream().findAny();

        if(discountMapping.isPresent()) {
            foodicsOrder.setDiscountId(discountMapping.get().getDiscountId().toString());
        }

        foodicsOrder.setKitchenNotes(talabatOrder.getOrderStatuses().get(0).getSendingToVendorDetails().getComment());
        foodicsOrder.setDueAt(parsedOrder.getOrderTimestamp());
        foodicsOrder.setTableId("1");
        foodicsOrder.setCustomerNotes("");
        foodicsOrder.setDriverId(parsedOrder.getDelivery().toString());
        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
        foodicsOrder.setCustomerAddressId(parsedOrder.getDelivery().getAddressText());
        foodicsOrder.setCustomerId(parsedOrder.getDelivery().getAddressText());
        foodicsOrder.setDe


    }
}

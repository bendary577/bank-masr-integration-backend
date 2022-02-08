package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Order;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
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

        }

        orderRepo.save(talabatOrder);

    }
}

package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.simphony.request.SimphonyPaymentReq;
import org.springframework.stereotype.Service;

@Service
public class SimphonyPaymentService {


    public Response createSimphonyPaymentTransaction(SimphonyPaymentReq simphonyPayment, Account account) {

        Response response = new Response();

        return response;
    }
}

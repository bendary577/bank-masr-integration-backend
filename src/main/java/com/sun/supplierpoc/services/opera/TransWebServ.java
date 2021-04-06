package com.sun.supplierpoc.services.opera;

import com.google.gson.JsonObject;
import com.sun.supplierpoc.models.opera.Transaction;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransWebServ {

    private final RestTemplate restTemplate;

    public TransWebServ(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Transaction transactionService(Transaction transaction) {

//        String url = "http://localhost:8081/2wlsIntegration/transaction";

//        Transaction transaction1 =
//                this.restTemplate.postForObject(url, transaction, Transaction.class);

        return transaction;
    }
}


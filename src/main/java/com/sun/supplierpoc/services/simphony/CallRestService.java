package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.models.simphony.check.ZealPayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CallRestService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);

    private final RestTemplate restTemplate;

    public CallRestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ZealPayment zealPayment(ZealPayment zealPayment) {
        String url = "http://localhost:8081/zeal/zealPayment/test";
        return this.restTemplate.postForObject(url, zealPayment, ZealPayment.class);
    }

}

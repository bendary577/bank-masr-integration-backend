package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.models.simphony.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.ZealRedeemResponse;
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

    public ZealLoyaltyResponse zealPayment(ZealLoyaltyRequest zealPayment) {
        String url = "https://private-anon-09bdbe61bf-symphonypos.apiary-mock.com/api/visit/scan";
        ZealLoyaltyResponse zealLoyaltyResponse =
                this.restTemplate.postForObject(url, zealPayment, ZealLoyaltyResponse.class);

        return zealLoyaltyResponse;

    }

    public ZealRedeemResponse zealVoucher(ZealRedeemRequest zealRedeemRequest) {

        String url = "https://private-anon-09bdbe61bf-symphonypos.apiary-mock.com/api/redeem/scan";

        ZealRedeemResponse zealRedeemResponse =
                this.restTemplate.postForObject(url, zealRedeemRequest, ZealRedeemResponse.class);

        logger.info(zealRedeemResponse.toString());

        return zealRedeemResponse;

    }

}

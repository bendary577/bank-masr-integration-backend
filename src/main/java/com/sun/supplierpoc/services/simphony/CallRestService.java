package com.sun.supplierpoc.services.simphony;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.supplierpoc.models.simphony.request.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.response.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.request.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.response.ZealRedeemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CallRestService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);

    @Autowired
    private RestTemplate restTemplate;

    public CallRestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ZealLoyaltyResponse zealPayment(ZealLoyaltyRequest zealPayment) {

        String url = "https://macros-pos-production.zeal-members.com/api/visit/scan";

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HttpHeaderInterceptor("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1"));
        this.restTemplate.setInterceptors(interceptors);
        ZealLoyaltyResponse zealLoyaltyResponse = new ZealLoyaltyResponse();
        try{
            zealLoyaltyResponse =
                this.restTemplate.postForObject(url, zealPayment, ZealLoyaltyResponse.class);
        }catch(Exception e){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                zealLoyaltyResponse = objectMapper.readValue(e.getMessage().substring(e.getMessage().indexOf("{"),
                        e.getMessage().indexOf("]")), new TypeReference<>() {  });
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
        }

        return zealLoyaltyResponse;
    }

    public ZealRedeemResponse zealVoucher(ZealRedeemRequest zealRedeemRequest) {

        String url = "https://macros-pos-production.zeal-members.com/api/redeem/scan";
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HttpHeaderInterceptor("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1"));
        this.restTemplate.setInterceptors(interceptors);

        ZealRedeemResponse zealRedeemResponse = new ZealRedeemResponse();

        try{
            zealRedeemResponse =
                    this.restTemplate.postForObject(url, zealRedeemRequest, ZealRedeemResponse.class);
        }catch(Exception e){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                zealRedeemResponse = objectMapper.readValue(e.getMessage().substring(e.getMessage().indexOf("{"),
                        e.getMessage().indexOf("]")), new TypeReference<>() {  });
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
        }


        return zealRedeemResponse;
    }

}

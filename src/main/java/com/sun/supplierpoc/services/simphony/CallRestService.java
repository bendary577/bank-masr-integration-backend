package com.sun.supplierpoc.services.simphony;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.supplierpoc.models.simphony.Message;
import com.sun.supplierpoc.models.simphony.request.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.response.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.request.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.response.ZealRedeemResponse;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CallRestService {

//    Logger logger = LoggerFactory.getLogger(CallRestService.class);
    @Autowired
    private RestTemplate restTemplate;

    public CallRestService(RestTemplateBuilder restTemplateBuilder) {
//        this.restTemplate = restTemplateBuilder.build();
    }

    public ZealLoyaltyResponse zealPayment(ZealLoyaltyRequest zealPayment) {
//        String url = "https://macros-pos-production.zeal-members.com/api/visit/scan";
//
//        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
////        interceptors.add(new HttpHeaderInterceptor("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1"));
//        this.restTemplate.setInterceptors(interceptors);
//        ZealLoyaltyResponse zealLoyaltyResponse = new ZealLoyaltyResponse();
//        try{
//            zealLoyaltyResponse =
//                this.restTemplate.postForObject(url, zealPayment, ZealLoyaltyResponse.class);
//        }catch(Exception e){
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                zealLoyaltyResponse = objectMapper.readValue(e.getMessage().substring(e.getMessage().indexOf("{"),
//                        e.getMessage().indexOf("]")), new TypeReference<>() {  });
//            } catch (JsonProcessingException jsonProcessingException) {
//                jsonProcessingException.printStackTrace();
//            }
//        }


        ZealLoyaltyResponse zealLoyaltyResponse = new ZealLoyaltyResponse();
        if(zealPayment.getVisitId().equals("123456789123456789")) {
            zealLoyaltyResponse = new ZealLoyaltyResponse("Paid Successfully", "111", true, true, "");
        }else if(zealPayment.getVisitId().equals("987654321987654321")) {
            zealLoyaltyResponse = new ZealLoyaltyResponse("Redeem added", "111", true, false, "");
        }else if(zealPayment.getVisitId().equals("123456789987654321")) {
            zealLoyaltyResponse = new ZealLoyaltyResponse("wrong id", "110", false, false, "");
        }

        return zealLoyaltyResponse;
    }

    public ZealRedeemResponse zealVoucher(ZealRedeemRequest zealRedeemRequest) {
//        String url = "https://macros-pos-production.zeal-members.com/api/redeem/scan";
//        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//
////        interceptors.add(new HttpHeaderInterceptor("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1"));
//        this.restTemplate.setInterceptors(interceptors);
//
//        ZealRedeemResponse zealRedeemResponse = new ZealRedeemResponse();
//
//        try{
//            zealRedeemResponse =
//                    this.restTemplate.postForObject(url, zealRedeemRequest, ZealRedeemResponse.class);
//        }catch(Exception e){
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                zealRedeemResponse = objectMapper.readValue(e.getMessage().substring(e.getMessage().indexOf("{"),
//                        e.getMessage().indexOf("]")), new TypeReference<>() {  });
//            } catch (JsonProcessingException jsonProcessingException) {
//                jsonProcessingException.printStackTrace();
//            }
//        }

        ZealRedeemResponse zealRedeemResponse = new ZealRedeemResponse();
        if(zealRedeemRequest.getUuid().equals("123456789123456789")) {
            zealRedeemResponse = new ZealRedeemResponse(new Message("Coffee added."), 111,"201000001" , true);
        }else if(zealRedeemRequest.getUuid().equals("987654321987654321")) {
            zealRedeemResponse = new ZealRedeemResponse(new Message("Wrong code"), 110,"" , false);
        }else if(zealRedeemRequest.getUuid().equals("12345678987654321")) {
            zealRedeemResponse = new ZealRedeemResponse(new Message("Coffee added"), 111,"31532" , false);
        }else if(zealRedeemRequest.getUuid().equals("123456789876543212")) {
            zealRedeemResponse = new ZealRedeemResponse(new Message("Coffee added"), 111,"1022" , false);
        }else if(zealRedeemRequest.getUuid().equals("1234567898765432123")) {
            zealRedeemResponse = new ZealRedeemResponse(new Message("Coffee added"), 111,"25960" , false);
        }

        return zealRedeemResponse;
    }

}

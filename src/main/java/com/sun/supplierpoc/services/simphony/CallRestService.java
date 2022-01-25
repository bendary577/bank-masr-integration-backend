package com.sun.supplierpoc.services.simphony;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.supplierpoc.models.simphony.Message;
import com.sun.supplierpoc.models.simphony.request.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.response.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.request.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.response.ZealRedeemResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class CallRestService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);

    @Autowired
    private final RestTemplate restTemplate;

    public CallRestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ZealLoyaltyResponse zealPayment(ZealLoyaltyRequest zealPayment) {

        String url = "https://macros-pos-production.zeal-members.com/api/visit/scan";

        ZealLoyaltyResponse zealLoyaltyResponse = new ZealLoyaltyResponse();

        try {
            Gson gson = new Gson(); // Or use new GsonBuilder().create();
            String json = gson.toJson(zealPayment); // serializes target to Json

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1")
                    .build();
            okhttp3.Response loginResponse = client.newCall(request).execute();


                try {
                    zealLoyaltyResponse = gson.fromJson(loginResponse.body().string(),
                            ZealLoyaltyResponse.class);
                } catch (Exception e) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        zealLoyaltyResponse = objectMapper.readValue(e.getMessage().substring(e.getMessage().indexOf("{"),
                                e.getMessage().indexOf("]")), new TypeReference<>() {
                        });
                    } catch (JsonProcessingException jsonProcessingException) {
                        jsonProcessingException.printStackTrace();
                    }
                }

        } catch (Exception e) {
            e.printStackTrace();
            zealLoyaltyResponse.setStatus("Fail");
            zealLoyaltyResponse.setMessage("Fail");
        }
        return zealLoyaltyResponse;
    }

    public ZealRedeemResponse zealVoucher(ZealRedeemRequest zealRedeemRequest) {


        String url = "https://macros-pos-production.zeal-members.com/api/redeem/scan";

        ZealRedeemResponse zealRedeemResponse = new ZealRedeemResponse();

        try {
            Gson gson = new Gson(); // Or use new GsonBuilder().create();
            String json = gson.toJson(zealRedeemRequest); // serializes target to Json

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("API-key", "xVOXeuZdwrpuNZsvx4G7Tul2dPLyYsy2iYhboWZFLGEY9O8lzwg5LzUmBeC8YiI1")
                    .build();
            okhttp3.Response loginResponse = client.newCall(request).execute();


            if(loginResponse.code() == 200) {
                zealRedeemResponse = gson.fromJson(loginResponse.body().string(),
                        ZealRedeemResponse.class);
            }else {

                HashMap response = gson.fromJson(loginResponse.body().string(),
                        HashMap.class);
                zealRedeemResponse.setCode((int) Double.parseDouble(response.get("code").toString()));
                zealRedeemResponse.setMessage(new Message(response.get("message").toString()));

            }

        } catch (Exception e) {
            e.printStackTrace();
            zealRedeemResponse.setStatus(false);
            zealRedeemResponse.setMessage(new Message(e.getMessage()));
        }

        return zealRedeemResponse;
    }

}

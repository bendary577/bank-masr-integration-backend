package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.Gson;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.simphony.request.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.talabat.TalabatOrder;
import com.sun.supplierpoc.models.talabat.Token;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class TalabatRestService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);


    public Response loginRequest() {

        Response response = new Response();
        String message = "";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,
                    "{\r\n\"email\": \"info@holmesburgers.com\"," +
                            "\r\n    \"password\": \"Ot@123456\"\r\n}");
            Request request = new Request.Builder()
                    .url("https://z2ib6nvxrj.execute-api.eu-west-1.amazonaws.com/prd/v3/master/login")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            okhttp3.Response loginResponse = client.newCall(request).execute();

//            OkHttpClient client = new OkHttpClient();
//            String url = "https://z2ib6nvxrj.execute-api.eu-west-1.amazonaws.com/prd/v3/master/login";
//            JSONObject json = new JSONObject();
//            String body = json.toString();
//            json.put("password", "Ot@123456");
//            json.put("email", "info@holmesburgers.com");
//            MediaType mediaType = MediaType.parse("application/json");
//            RequestBody requestBody = RequestBody.create(mediaType, body);
//            Request request = new Request.Builder().url(url).post(requestBody)
//                    .addHeader("content-type", "application/json")
////                    .addHeader("Authorization", credential)
//                    .build();
//
//            okhttp3.Response bookingResponse = client.newCall(request).execute();
            if (loginResponse.code() == 200){
                Gson gson = new Gson();
                Token token = gson.fromJson(loginResponse.body().string(),
                        Token.class);

                response.setStatus(true);
                response.setData(token);

            }else {
                message = loginResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            response.setStatus(false);
            response.setMessage(message);
        }

        return response;
    }


    public Response getOrders(Token token) {

        Response response = new Response();
        String message = "";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\": [\r\n        \"HF_EG;644819\",\r\n        \"HF_EG;512662\",\r\n        \"HF_EG;510696\",\r\n        \"HF_EG;510706\",\r\n        \"HF_EG;510705\",\r\n        \"HF_EG;514804\",\r\n        \"HF_EG;514803\",\r\n        \"HF_EG;510702\",\r\n        \"HF_EG;512663\",\r\n        \"HF_EG;510703\",\r\n        \"HF_EG;510701\",\r\n        \"HF_EG;625008\",\r\n        \"HF_EG;611425\"\r\n    ],\r\n    \"time_from\": \"2022-01-24T00:00:00+02:00\",\r\n    \"time_to\": \"2022-01-24T23:59:59+02:00\"\r\n}");
            Request request = new Request.Builder()
                    .url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders")
                    .method("POST", body)
                    .addHeader("Authorization", "Bearer " + token.getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();
            okhttp3.Response orderResponse = client.newCall(request).execute();

            if (orderResponse.code() == 200){
                Gson gson = new Gson();

                TalabatOrder talabatOrders = gson.fromJson(orderResponse.body().string(),
                        TalabatOrder.class);


                response.setStatus(true);
                response.setData(talabatOrders);

            }else {
                message = orderResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            response.setStatus(false);
            response.setMessage(message);
        }

        return response;
    }

    public Response getOrderById(TalabatOrder.Order order, Token token) {

        Response response = new Response();
        String message = "";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/" +
                            order.getGlobal_vendor_code() +
                            "/orders/" +
                            order.getOrder_id() +
                            "?order_timestamp=" +
                            order.getOrder_timestamp()
                    )
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + token.getAccessToken())
                    .build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            if (orderResponse.code() == 200){
                Gson gson = new Gson();

                HashMap talabatOrders = gson.fromJson(orderResponse.body().string(),
                        HashMap.class);


                response.setStatus(true);
                response.setData(talabatOrders);

            }else {
                message = orderResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            response.setStatus(false);
            response.setMessage(message);
        }

        return response;
    }
}

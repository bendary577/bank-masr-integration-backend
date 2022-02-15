package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.talabat.FoodicsProduct;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

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


    public Response getOrders(Token token , String branch) {

        Response response = new Response();
        String message = "";

        try {

            Date date = new Date();

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\": [\r\n" +
                    "        \"" + branch +"\"\r\n    ],\r\n" +
                    "    \"time_from\": \""+ getDate() +"T00:00:00+02:00\",\r\n" +
                    "    \"time_to\": \""+ getDate() +"T23:59:59+02:00\"\r\n}");

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


    public Response getOrders(Token token) {

        Response response = new Response();
        String message = "";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\":" +
                    " [\r\n        \"HF_EG;644819\",\r\n " +
                    "       \"HF_EG;512662\",\r\n" +
                    "        \"HF_EG;510696\",\r\n" +
                    "        \"HF_EG;510706\",\r\n" +
                    "        \"HF_EG;510705\",\r\n" +
                    "        \"HF_EG;514804\",\r\n" +
                    "        \"HF_EG;514803\",\r\n" +
                    "        \"HF_EG;510702\",\r\n" +
                    "        \"HF_EG;512663\",\r\n" +
                    "        \"HF_EG;510703\",\r\n" +
                    "        \"HF_EG;510701\",\r\n" +
                    "        \"HF_EG;625008\",\r\n" +
                    "        \"HF_EG;611425\"\r\n    ],\r\n" +
                    "    \"time_from\": \""+ getDate() +"T00:00:00+02:00\",\r\n" +
                    "    \"time_to\": \""+ getDate() +"T23:59:59+02:00\"\r\n}");
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

    public Response getOrderById(RestOrder order, Token token) {

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

                Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();

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

    public String getDate(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sdf.format(date);
    }


    public Response fetchProducts(Account account) {

        OkHttpClient client = new OkHttpClient();

        String url = "http://api-sandbox.foodics.com/v5/products";

        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjExMjdjNWQ3MDgzMTdiYzNlOTgzZWYzNWFlZWI3Y2RlYzg5YTE0" +
                "NGQzZjdiNzlmNzgyZTFmNDdkMzQ0YmIwZTYyN2M3MGM1MzBmOWYyMWIxIn0.eyJhdWQiOiI5NTkwZTZkZS0yNWRiLTRkZDM" +
                "tOGU0NS0zOTBkM2U2NzE2N2QiLCJqdGkiOiIxMTI3YzVkNzA4MzE3YmMzZTk4M2VmMzVhZWViN2NkZWM4OWExNDRkM2Y3Yjc5Zj" +
                "c4MmUxZjQ3ZDM0NGJiMGU2MjdjNzBjNTMwZjlmMjFiMSIsImlhdCI6MTY0NDgzNzUxMywibmJmIjoxNjQ0ODM3NTEzLCJleHAiOjE4MD" +
                "I2MDM5MTMsInN1YiI6Ijk1OTBlNTdlLTU3NDgtNGU2Yi1hOThmLWYzMmQwMzFkNjdkZCIsInNjb3BlcyI6W10sImJ1c2luZXNzIjoiOT" +
                "U5MGU1N2UtNjYzZS00MWZiLWE4MWUtOTA3ZGUxNmVmMmU1IiwicmVmZXJlbmNlIjoiMTM5NzA0In0.gD5Bkcq1PrIayZwXBaNBX0n2yxA1ayN8t9xc3T3" +
                "gDACl5YH4ii38Kvdr7o3PFcZqiqZyE2ag3ucMEHXmD0SxOon0Iq25m-Il8acE1YHH_iOS0YezRqdI-2X1JwYfFmFzfSxw8PFT6_ixBUYLPD3l97YTD5l1KA0-4fD" +
                "nrJVx3x4905iuViOkV6w1Z_3PLfDGdiIVnYvHGFXrmGeX7S4Ts2wUM1TbLfi7WRqARKl4jZdDWf4xLwgKMd7l2whcMgN-2xXBuSpdVIVmHphbha_JqIIm4YA" +
                "6cFTpqFxPJGqNSNFYlxUv8G3KitY2sqitKl9EJalTEOS3FUGJEaL5xN-W83TDPHjdjWfTOsKOQRvEx8vs3Joqp4TT6smBGHkZSs1Ox2tqT8-X2Y4uEXmjeDi4zwWzvzi" +
                "zgvBDApnCdtE8kwQo9qkbEN2MG7GO7OCfMVk-VFazEKxnmX1eWdf_0Z4_yHUhrxNSiEsfRLc80RRoEecoco2HbUHnAC2jl00au2U6gQCXQIYm0qZ4fnd8aEzAtYb1BNj03bo0Z-9YoyRQ1V9JiXt" +
                "f43xq0fIMZjUejUxahVGdaa5Zf5qQUvaWJ3X2sIQIRLwvAkJmx8ecXFpOU-VSM0JtYSfwadZJY-y76wBlC61QQpuXIfr3oTgWg5fLZhnF9iLKQoa4P54epSX-EaU";


        Request request = new Request.Builder()
                .url(url).get()
                .addHeader("authorization", "Bearer " + token)
                .addHeader("cache-control", "no-cache")
//                .addHeader("postman-token", "1b6e5077-4c4c-98fc-3d0a-061b34579579")
                .build();

        try {
            okhttp3.Response response = client.newCall(request).execute();

            if (response.code() == 200){

                Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();

                TalabatOrder talabatOrders = gson.fromJson(orderResponse.body().string(),
                        TalabatOrder.class);


                response.setStatus(true);
                response.setData(talabatOrders);

            }else {
                message = orderResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new FoodicsProduct();
    }

}

package com.sun.supplierpoc.services.restTemplate;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.configurations.TalabatConfiguration;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccount;
import com.sun.supplierpoc.models.talabat.FoodicProductResponse;
import com.sun.supplierpoc.models.talabat.FoodicsProduct;
import com.sun.supplierpoc.models.talabat.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.talabat.TalabatRest.TalabatOrder;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsLoginBody;
import com.sun.supplierpoc.models.talabat.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.talabat.login.Token;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalabatRestService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);


    public Token talabatLoginRequest(Account account) {

        Token token = new Token();

        try {

            String url = "https://z2ib6nvxrj.execute-api.eu-west-1.amazonaws.com/prd/v3/master/login";

            AccountCredential credentials = account.getAccountCredentials().stream().filter(c -> c.getAccount().equals("Talabat")).collect(Collectors.toSet()).stream().findAny().orElse(null);

            if (credentials == null) {
                token.setStatus(false);
                token.setMessage("No credentials found");
                return token;
            }

            Gson gson = new Gson();
            String jsonCredential = gson.toJson(credentials);

            OkHttpClient client = new OkHttpClient().newBuilder().build();

            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, jsonCredential);
            Request request = new Request.Builder().url(url).method("POST", body).addHeader("Content-Type", "application/json").build();

            okhttp3.Response loginResponse = client.newCall(request).execute();

            gson = new Gson();

            token = gson.fromJson(loginResponse.body().string(), Token.class);

            token.setStatus(loginResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            token.setMessage(e.getMessage());
            token.setStatus(false);
        }

        return token;
    }


    public TalabatOrder getOrders(Token token, String branch) {

        TalabatOrder talabatOrders = new TalabatOrder();
        String message = "";

        try {

            Date date = new Date();

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\": [\r\n" + "        \"" + branch + "\"\r\n    ],\r\n" + "    \"time_from\": \"" + getDate() + "T00:00:00+02:00\",\r\n" + "    \"time_to\": \"" + getDate() + "T23:59:59+02:00\"\r\n}");

            Request request = new Request.Builder().url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders").method("POST", body).addHeader("Authorization", "Bearer " + token.getAccessToken()).addHeader("Content-Type", "application/json").build();
            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    public TalabatOrder getOrders(Token token) {

        TalabatOrder talabatOrders = new TalabatOrder();
        String url = "https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders";
        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\":" + " [\r\n        \"HF_EG;644819\",\r\n " + "       \"HF_EG;512662\",\r\n" + "        \"HF_EG;510696\",\r\n" + "        \"HF_EG;510706\",\r\n" + "        \"HF_EG;510705\",\r\n" + "        \"HF_EG;514804\",\r\n" + "        \"HF_EG;514803\",\r\n" + "        \"HF_EG;510702\",\r\n" + "        \"HF_EG;512663\",\r\n" + "        \"HF_EG;510703\",\r\n" + "        \"HF_EG;510701\",\r\n" + "        \"HF_EG;625008\",\r\n" + "        \"HF_EG;611425\"\r\n    ],\r\n" + "    \"time_from\": \"" + getDate() + "T00:00:00+02:00\",\r\n" + "    \"time_to\": \"" + getDate() + "T23:59:59+02:00\"\r\n}");
            Request request = new Request.Builder().url(url).method("POST", body).addHeader("Authorization", "Bearer " + token.getAccessToken()).addHeader("Content-Type", "application/json").build();

            okhttp3.Response orderResponse = client.newCall(request).execute();


            Gson gson = new Gson();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    public TalabatOrder getOrderById(RestOrder order, Token token) {

        TalabatOrder talabatOrders = new TalabatOrder();

        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/" + order.getGlobal_vendor_code() + "/orders/" + order.getOrder_id() + "?order_timestamp=" + order.getOrder_timestamp()).method("GET", null).addHeader("Authorization", "Bearer " + token.getAccessToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

//    public FoodicsLoginBody LoginToFoodics() {
//
//        FoodicsLoginBody foodicsLoginBody = new FoodicsLoginBody();
//        String url = "https://api-sandbox.foodics.com/oauth/token";
//        try {
//
//            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
//            String jsonRequest = gson.toJson(foodicsLoginBody);
//
//            OkHttpClient client = new OkHttpClient().newBuilder().build();
//            MediaType mediaType = MediaType.parse("Application/json");
//            RequestBody body = RequestBody.create(mediaType, jsonRequest);
//
//            Request request = new Request.Builder().url(url).method("POST", body).
//                    addHeader("Content-Type", "Application/json").build();
//
//            okhttp3.Response foodicsLoginResponse = client.newCall(request).execute();
//
//            foodicsLoginBody = gson.fromJson(foodicsLoginResponse.body().string(), FoodicsLoginBody.class);
//
//            if (foodicsLoginResponse.code() == 200) {
//                foodicsLoginBody.setStatus(true);
//
//            } else {
//                foodicsLoginBody.setStatus(false);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            foodicsLoginBody.setMessage( e.getMessage() );
//            foodicsLoginBody.setStatus(false);
//        }
//
//        return foodicsLoginBody;
//    }

    public FoodicsOrder sendOrderToFoodics(FoodicsOrder order, FoodicsLoginBody token, GeneralSettings generalSettings,
                                           FoodicsAccount foodicsAccount) {

        FoodicsOrder foodicsOrder = new FoodicsOrder();
        String url = "https://api-sandbox.foodics.com/v5/orders";
        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            String jsonOrder = gson.toJson(order);

            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, jsonOrder);
            Request request = new Request.Builder().url(url).method("POST", body).
                    addHeader("Authorization", "Bearer " + foodicsAccount.getToken()).
                    addHeader("Content-Type", "application/json").
                    addHeader("Accept", "application/json").build();

            okhttp3.Response foodicsResponse = client.newCall(request).execute();

//            foodicsOrder = gson.fromJson(foodicsResponse.body().string(), FoodicsOrder.class);
            HashMap<String, String> hashMap = gson.fromJson(foodicsResponse.body().string(), HashMap.class);

            if (foodicsResponse.code() == 200) {
                foodicsOrder.setStatus(true);

            } else {
                foodicsOrder.setStatus(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            foodicsOrder.setMessage(e.getMessage());
            foodicsOrder.setStatus(false);
        }

        return foodicsOrder;
    }

    public Response fetchProducts(GeneralSettings generalSettings, FoodicsAccount foodicsAccount) {
        Response response = new Response();
        String url = "https://api-sandbox.foodics.com/v5/products";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url("https://api-sandbox.foodics.com/v5/products")
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccount.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();
            okhttp3.Response getProductsResponse = client.newCall(request).execute();
            if (getProductsResponse.code() == 200) {
                Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();
                FoodicProductResponse foodicsProductsResponse1 = gson.fromJson(getProductsResponse.body().string(),
                        FoodicProductResponse.class);

                response.setFoodicsProducts(foodicsProductsResponse1.getData());
                response.setStatus(true);
                return response;
            } else {
                response.setMessage("Can't fetch products data.");
                response.setStatus(false);
                return response;
            }
        } catch (IOException e) {
            response.setMessage("Can't fetch products data due to error: " + e.getMessage());
            response.setStatus(false);
            return response;
        }
    }


    public String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sdf.format(date);
    }

}

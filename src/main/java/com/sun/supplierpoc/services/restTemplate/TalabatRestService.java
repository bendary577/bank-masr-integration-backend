package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.TalabatAggregatorOrder;
import com.sun.supplierpoc.models.aggregtor.login.Token;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public TalabatAggregatorOrder getOrders(Token token, String branch) {

        TalabatAggregatorOrder talabatOrders = new TalabatAggregatorOrder();
        String message = "";

        try {

            Date date = new Date();

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\r\n    \"global_vendor_codes\": [\r\n" + "        \"" + branch + "\"\r\n    ],\r\n" + "    \"time_from\": \"" + getDate() + "T00:00:00+02:00\",\r\n" + "    \"time_to\": \"" + getDate() + "T23:59:59+02:00\"\r\n}");

            Request request = new Request.Builder().url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders")
                    .method("POST", body).addHeader("Authorization", "Bearer " + token.getAccessToken())
                    .addHeader("Content-Type", "application/json").build();
            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatAggregatorOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    public TalabatAggregatorOrder getOrders(Token token, ArrayList<String> branches) {

        TalabatAggregatorOrder talabatOrders = new TalabatAggregatorOrder();
        String url = "https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders";
        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");

            JSONObject json = new JSONObject();
            json.put("global_vendor_codes", branches);
            json.put("time_from", getDate() + "T00:00:00+02:00");
            json.put("time_to", getDate() + "T23:59:59+02:00");

            RequestBody body = RequestBody.create(mediaType, json.toString());
            Request request = new Request.Builder().url(url).method("POST", body).addHeader("Authorization", "Bearer " + token.getAccessToken()).addHeader("Content-Type", "application/json").build();

            okhttp3.Response orderResponse = client.newCall(request).execute();


            Gson gson = new Gson();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatAggregatorOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    public TalabatAggregatorOrder getOrdersbyBranch(Token token, String branch) {

        TalabatAggregatorOrder talabatOrders = new TalabatAggregatorOrder();
        String url = "https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/orders";
        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");

            JSONObject json = new JSONObject();
            ArrayList<String> branches = new ArrayList<>();
            branches.add(branch);

            json.put("global_vendor_codes", branches);
            json.put("time_from", getDate() + "T00:00:00+02:00");
            json.put("time_to", getDate() + "T23:59:59+02:00");

            RequestBody body = RequestBody.create(mediaType, json.toString());
            Request request = new Request.Builder().url(url).method("POST", body).addHeader("Authorization", "Bearer " + token.getAccessToken()).addHeader("Content-Type", "application/json").build();

            okhttp3.Response orderResponse = client.newCall(request).execute();


            Gson gson = new Gson();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatAggregatorOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    /*
    * Get order detials (items, discount, deliery fees, amount)
    * */
    public TalabatAggregatorOrder getOrderById(RestOrder order, Token token) {

        TalabatAggregatorOrder talabatOrders = new TalabatAggregatorOrder();

        try {

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url("https://os-backend.api.eu.prd.portal.restaurant/v1/vendors/" + order.getGlobal_vendor_code() + "/orders/" + order.getOrder_id() + "?order_timestamp=" + order.getOrder_timestamp()).method("GET", null).addHeader("Authorization", "Bearer " + token.getAccessToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

            talabatOrders = gson.fromJson(orderResponse.body().string(), TalabatAggregatorOrder.class);

            talabatOrders.setStatus(orderResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            talabatOrders.setMessage(e.getMessage());
            talabatOrders.setStatus(false);
        }

        return talabatOrders;
    }

    public String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sdf.format(date);
    }

}

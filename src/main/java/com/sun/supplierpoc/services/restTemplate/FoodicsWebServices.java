package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Product;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.AggregatorConstants;
import com.sun.supplierpoc.models.aggregtor.FoodicsAccessToken;
import com.sun.supplierpoc.models.aggregtor.FoodicsAccessTokenRequest;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminFailedResponse;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminToken;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.FoodicProductResponse;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FoodicsWebServices {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);

    private static final String BASE_URL = "";

    public FoodicsOrder sendOrderToFoodics(FoodicsOrder order, GeneralSettings generalSettings,
                                           FoodicsAccountData foodicsAccountData) {

        FoodicsOrder foodicsOrder = order;
        String url = "https://api-sandbox.foodics.com/v5/orders";

        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            String jsonOrder = gson.toJson(order);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonOrder);
            Request request = new Request.Builder()
                    .url(url).method("POST", body)   .
                    addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build();

            okhttp3.Response foodicsResponse = client.newCall(request).execute();

            CreateOrderRequest createOrderRequest = gson.fromJson(foodicsResponse.body().string(), CreateOrderRequest.class);

            if (foodicsResponse.code() == 200) {
                foodicsOrder = createOrderRequest.getData();
                foodicsOrder.setCallStatus(true);
            } else {
//                FoodicsFailedResponse failedResponse = gson.fromJson(foodicsResponse.body().string(), FoodicsFailedResponse.class);

                foodicsOrder.setCallStatus(false);
                foodicsOrder.setMessage(createOrderRequest.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            foodicsOrder.setMessage(e.getMessage());
            foodicsOrder.setCallStatus(false);
        }

        return foodicsOrder;
    }

    public Product fetchProducts(GeneralSettings generalSettings, FoodicsAccountData foodicsAccountData) {
        Product product = new Product();
        String url = "https://api-sandbox.foodics.com/v5/products";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getProductsResponse = client.newCall(request).execute();
            if (getProductsResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicProductResponse foodicsProductsResponse = gson.fromJson(getProductsResponse.body().string(), FoodicProductResponse.class);
                product.setFoodicsProducts(foodicsProductsResponse.getData());
                product.setType(AggregatorConstants.FOODICS);
                return product;
            } else {
                return product;
            }
        } catch (IOException e) {
            return product;
        }
    }

    public List<FoodicsProduct> fetchFoodicsProducts(FoodicsAccountData foodicsAccountData) {
        ArrayList<FoodicsProduct> foodicsProducts = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/products";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getProductsResponse = client.newCall(request).execute();
            if (getProductsResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicProductResponse foodicsProductsResponse = gson.fromJson(getProductsResponse.body().string(), FoodicProductResponse.class);
                foodicsProducts.addAll(foodicsProductsResponse.getData());
                return foodicsProducts;
            } else {
                return foodicsProducts;
            }
        } catch (IOException e) {
            return foodicsProducts;
        }
    }

    public FoodicsAccessToken getFoodicsAccessToken(FoodicsAccessTokenRequest foodicsAccessTokenRequest) {
        FoodicsAccessToken foodicsAccessToken = new FoodicsAccessToken();
        String url = "https://api-sandbox.foodics.com/oauth/token";
        String requestBody = "";
        try {
            requestBody = "{\n" +
                    "    \"grant_type\": \"authorization_code\",\n" +
                    "    \"code\":" + "\"" + foodicsAccessTokenRequest.getCode() + "\"" + ",\n" +
                    "    \"client_id\":" + "\"" + foodicsAccessTokenRequest.getClientId() + "\"" + ",\n" +
                    "    \"client_secret\":" + "\"" + foodicsAccessTokenRequest.getClientSecret() + "\"" + ",\n" +
                    "    \"redirect_uri\":" + "\"" + foodicsAccessTokenRequest.getRedirect_url() + "\"" + "\n" +
                    "}";
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBody);
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url).method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json").build();
            okhttp3.Response getTokenResponse = client.newCall(request).execute();
            if (getTokenResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                 foodicsAccessToken = gson.fromJson(getTokenResponse.body().string(), FoodicsAccessToken.class);
                 foodicsAccessToken.setStatus(getTokenResponse.code() == 200);
                 return foodicsAccessToken;
            } else {
                foodicsAccessToken.setStatus(getTokenResponse.code() == 200);
                foodicsAccessToken.setMessage("Failed to generate foodics access token");
                return foodicsAccessToken;
            }
        } catch (IOException e) {
            return foodicsAccessToken;
        }
    }
}

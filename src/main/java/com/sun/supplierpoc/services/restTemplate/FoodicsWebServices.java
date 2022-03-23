package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Product;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminFailedResponse;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsFailedResponse;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.models.aggregtor.FoodicProductResponse;
import com.sun.supplierpoc.models.aggregtor.foodics.CreateOrderRequest;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsLoginBody;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
                product.setType(Constants.FOODICS);
                return product;
            } else {
                return product;
            }
        } catch (IOException e) {
            return product;
        }
    }


}

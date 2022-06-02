package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.Product;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.aggregtor.*;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminFailedResponse;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminToken;
import com.sun.supplierpoc.models.aggregtor.foodics.*;
import com.sun.supplierpoc.models.configurations.foodics.FoodicsAccountData;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FoodicsWebServices {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);

    private static final String BASE_URL = "";

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

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


    //------------------------------ foodics authentication -------------------------------------
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

    //------------------------------ saving foodics products, modifiers and branches -------------------------------------

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

    public FoodicProductResponse fetchFoodicsProducts(FoodicsAccountData foodicsAccountData) {
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
//                foodicsProducts.addAll(foodicsProductsResponse.getData());
                foodicsProductsResponse.setStatus(true);
                foodicsProductsResponse.setMessage("Foodics products returned successfully");
                return foodicsProductsResponse;
            } else {
                FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
                foodicsProductsResponse.setStatus(false);
                foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
                return foodicsProductsResponse;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
            foodicsProductsResponse.setStatus(false);
            foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
            return foodicsProductsResponse;
        }
    }

    public List<FoodicsBranch> fetchFoodicsBranches(FoodicsAccountData foodicsAccountData) {
        ArrayList<FoodicsBranch> foodicsBranches = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/branches";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getBranchesResponse = client.newCall(request).execute();
            if (getBranchesResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicBranchResponse foodicBranchResponse = gson.fromJson(getBranchesResponse.body().string(), FoodicBranchResponse.class);
                foodicsBranches.addAll(foodicBranchResponse.getData());
                return foodicsBranches;
            } else {
                return foodicsBranches;
            }
        } catch (IOException e) {
            return foodicsBranches;
        }
    }

    public List<FoodicsModifier> fetchFoodicsModifiers(FoodicsAccountData foodicsAccountData) {
        ArrayList<FoodicsModifier> foodicsModifiers = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/modifiers";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getModifiersResponse = client.newCall(request).execute();
            if (getModifiersResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = gson.fromJson(getModifiersResponse.body().string(), FoodicPaginatedModifierResponse.class);
//                foodicsProducts.addAll(foodicsProductsResponse.getData());
                foodicPaginatedModifierResponse.setStatus(true);
                foodicPaginatedModifierResponse.setMessage("Foodics products returned successfully");
                return foodicsModifiers;
            } else {
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = new FoodicPaginatedModifierResponse();
                foodicPaginatedModifierResponse.setStatus(false);
                foodicPaginatedModifierResponse.setMessage("Error occurred while trying to fetch foodics products");
                return foodicsModifiers;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
            foodicsProductsResponse.setStatus(false);
            foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
            return foodicsModifiers;
        }
    }

    //------------------------------ saving foodics paginated products and modifiers -------------------------------------

    public FoodicProductResponse fetchFoodicsProductsPaginated(FoodicsAccountData foodicsAccountData, String requestAPI) {
        ArrayList<FoodicsProduct> foodicsProducts = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/products";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(requestAPI).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getProductsResponse = client.newCall(request).execute();
            if (getProductsResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicProductResponse foodicsProductsResponse = gson.fromJson(getProductsResponse.body().string(), FoodicProductResponse.class);
//                foodicsProducts.addAll(foodicsProductsResponse.getData());
                foodicsProductsResponse.setStatus(true);
                foodicsProductsResponse.setMessage("Foodics products returned successfully");
                return foodicsProductsResponse;
            } else {
                FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
                foodicsProductsResponse.setStatus(false);
                foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
                return foodicsProductsResponse;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
            foodicsProductsResponse.setStatus(false);
            foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
            return foodicsProductsResponse;
        }
    }

    public FoodicPaginatedModifierResponse fetchFoodicsModifiersPaginated(FoodicsAccountData foodicsAccountData, String requestAPI) {
        ArrayList<FoodicsModifier> foodicsModifiers = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/modifiers";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(requestAPI).method("GET", null)
                    .addHeader("Authorization", "Bearer " + foodicsAccountData.getToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getModifiersResponse = client.newCall(request).execute();
            if (getModifiersResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = gson.fromJson(getModifiersResponse.body().string(), FoodicPaginatedModifierResponse.class);
                foodicPaginatedModifierResponse.setStatus(true);
                foodicPaginatedModifierResponse.setMessage("Foodics modifiers returned successfully");
                return foodicPaginatedModifierResponse;
            } else {
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = new FoodicPaginatedModifierResponse();
                foodicPaginatedModifierResponse.setStatus(false);
                foodicPaginatedModifierResponse.setMessage("Error occurred while trying to fetch foodics products");
                return foodicPaginatedModifierResponse;
            }
        } catch (IOException e) {
//            e.printStackTrace();
            FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = new FoodicPaginatedModifierResponse();
            foodicPaginatedModifierResponse.setStatus(false);
            foodicPaginatedModifierResponse.setMessage("Error occurred while trying to fetch foodics modifiers");
            return foodicPaginatedModifierResponse;
        }
    }

    //------------------------------ saving all foodics products and modifiers -------------------------------------

    public Response getAllFoodicsProducts(GeneralSettings generalSettings) {
        Response response = new Response();
        int callNumber=1;
        int numberOfPage = 0;
        boolean allProductsFetched = true;
        String token = generalSettings.getAggregatorConfiguration().getFoodicsAccountData().getToken();
        ArrayList<FoodicsProduct> foodicsProducts = new ArrayList<>();
        String url = "https://api-sandbox.foodics.com/v5/products?page=";
        FoodicProductResponse foodicProductResponse = callFoodicsProductsAPI(token, url, callNumber);
        if(foodicProductResponse.isStatus()){
            generalSettings.getAggregatorConfiguration().getFoodicsDropDownProducts().addAll(foodicProductResponse.getData());
            numberOfPage = foodicProductResponse.getMeta().getLast_page();
            for(int i=2 ; i<=numberOfPage; i++){
                foodicProductResponse = callFoodicsProductsAPI(token, url, i);
                if(foodicProductResponse.isStatus()){
                    generalSettings.getAggregatorConfiguration().getFoodicsDropDownProducts().addAll(foodicProductResponse.getData());
                }else{
                    allProductsFetched = false;
                }
            }
            if(allProductsFetched == false){
                response.setStatus(false);
                response.setMessage("There was an error while calling foodics webservice, not all products were saved");
            }else{
                generalSettingsRepo.save(generalSettings);
                response.setStatus(true);
                response.setMessage("all foodics products saved successfully");
            }
        }else{
            allProductsFetched = false;
            response.setStatus(false);
            response.setMessage("Sorry, there was an error while calling foodics web service");
        }
        return response;
    }

    public Response getAllFoodicsModifiers(GeneralSettings generalSettings) {
        Response response = new Response();
        int callNumber=1;
        int numberOfPage = 0;
        boolean allModifiersFetched = true;
        String token = generalSettings.getAggregatorConfiguration().getFoodicsAccountData().getToken();
        String url = "https://api-sandbox.foodics.com/v5/modifiers?page=";
        FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = callFoodicsModifiersAPI(token, url, callNumber);
        if(foodicPaginatedModifierResponse.isStatus()){
            generalSettings.getAggregatorConfiguration().getFoodicsDropDownModifiers().addAll(foodicPaginatedModifierResponse.getData());
            numberOfPage = foodicPaginatedModifierResponse.getMeta().getLast_page();
            for(int i=2 ; i<=numberOfPage; i++){
                foodicPaginatedModifierResponse = callFoodicsModifiersAPI(token, url, i);
                if(foodicPaginatedModifierResponse.isStatus()){
                    generalSettings.getAggregatorConfiguration().getFoodicsDropDownModifiers().addAll(foodicPaginatedModifierResponse.getData());
                }else{
                    allModifiersFetched = false;
                }
            }
            if(allModifiersFetched == false){
                response.setStatus(false);
                response.setMessage("There was an error while calling foodics webservice, not all modifiers were saved");
            }else{
                generalSettingsRepo.save(generalSettings);
                response.setStatus(true);
                response.setMessage("all foodics modifiers saved successfully");
            }
        }else{
            allModifiersFetched = false;
            response.setStatus(false);
            response.setMessage("Sorry, there was an error while calling foodics modifiers web service");
        }
        return response;
    }

    public Response getAllFoodicsBranches(GeneralSettings generalSettings) {
        Response response = new Response();
        int callNumber=1;
        int numberOfPage = 0;
        boolean allBranchesFetched = true;
        String token = generalSettings.getAggregatorConfiguration().getFoodicsAccountData().getToken();
        String url = "https://api-sandbox.foodics.com/v5/branches?page=";
        FoodicPaginatedBranchResponse foodicPaginatedBranchResponse = callFoodicsBranchesAPI(token, url, callNumber);
        if(foodicPaginatedBranchResponse.isStatus()){
            generalSettings.getAggregatorConfiguration().getFoodicsDropDownBranches().addAll(foodicPaginatedBranchResponse.getData());
            numberOfPage = foodicPaginatedBranchResponse.getMeta().getLast_page();
            if(numberOfPage > 1){
                for(int i=2 ; i<=numberOfPage; i++){
                    foodicPaginatedBranchResponse = callFoodicsBranchesAPI(token, url, i);
                    if(foodicPaginatedBranchResponse.isStatus()){
                        generalSettings.getAggregatorConfiguration().getFoodicsDropDownBranches().addAll(foodicPaginatedBranchResponse.getData());
                    }else{
                        allBranchesFetched = false;
                    }
                }
            }
            if(allBranchesFetched == false){
                response.setStatus(false);
                response.setMessage("There was an error while calling foodics webservice, not all branches were saved");
            }else{
                generalSettingsRepo.save(generalSettings);
                response.setStatus(true);
                response.setMessage("all foodics branches saved successfully");
            }
        }else{
            allBranchesFetched = false;
            response.setStatus(false);
            response.setMessage("Sorry, there was an error while calling foodics modifiers web service");
        }
        return response;
    }


    public FoodicProductResponse callFoodicsProductsAPI(String token, String url, int callNumber) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url+callNumber).method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getProductsResponse = client.newCall(request).execute();
            if (getProductsResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicProductResponse foodicsProductsResponse = gson.fromJson(getProductsResponse.body().string(), FoodicProductResponse.class);
                foodicsProductsResponse.setStatus(true);
                foodicsProductsResponse.setMessage("Foodics products returned successfully");
                return foodicsProductsResponse;
            } else {
                FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
                foodicsProductsResponse.setStatus(false);
                foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
                return foodicsProductsResponse;
            }
        } catch (IOException e) {
            FoodicProductResponse foodicsProductsResponse = new FoodicProductResponse();
            foodicsProductsResponse.setStatus(false);
            foodicsProductsResponse.setMessage("Error occurred while trying to fetch foodics products");
            return foodicsProductsResponse;
        }
    }

    public FoodicPaginatedModifierResponse callFoodicsModifiersAPI(String token, String url, int callNumber) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url+callNumber).method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getModifiersResponse = client.newCall(request).execute();
            if (getModifiersResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = gson.fromJson(getModifiersResponse.body().string(), FoodicPaginatedModifierResponse.class);
                foodicPaginatedModifierResponse.setStatus(true);
                foodicPaginatedModifierResponse.setMessage("Foodics modifiers returned successfully");
                return foodicPaginatedModifierResponse;
            } else {
                FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = new FoodicPaginatedModifierResponse();
                foodicPaginatedModifierResponse.setStatus(false);
                foodicPaginatedModifierResponse.setMessage("Error occurred while trying to fetch foodics modifiers");
                return foodicPaginatedModifierResponse;
            }
        } catch (IOException e) {
            FoodicPaginatedModifierResponse foodicPaginatedModifierResponse = new FoodicPaginatedModifierResponse();
            foodicPaginatedModifierResponse.setStatus(false);
            foodicPaginatedModifierResponse.setMessage("Error occurred while trying to fetch foodics modifiers");
            return foodicPaginatedModifierResponse;
        }
    }

    public FoodicPaginatedBranchResponse callFoodicsBranchesAPI(String token, String url, int callNumber) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder()
                    .url(url+callNumber).method("GET", null)
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json") .build();

            okhttp3.Response getBrachesResponse = client.newCall(request).execute();
            if (getBrachesResponse.code() == 200) {
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                FoodicPaginatedBranchResponse foodicPaginatedBranchResponse = gson.fromJson(getBrachesResponse.body().string(), FoodicPaginatedBranchResponse.class);
                foodicPaginatedBranchResponse.setStatus(true);
                foodicPaginatedBranchResponse.setMessage("Foodics branches returned successfully");
                return foodicPaginatedBranchResponse;
            } else {
                FoodicPaginatedBranchResponse foodicPaginatedBranchResponse = new FoodicPaginatedBranchResponse();
                foodicPaginatedBranchResponse.setStatus(false);
                foodicPaginatedBranchResponse.setMessage("Error occurred while trying to fetch foodics branches");
                return foodicPaginatedBranchResponse;
            }
        } catch (IOException e) {
            FoodicPaginatedBranchResponse foodicPaginatedBranchResponse = new FoodicPaginatedBranchResponse();
            foodicPaginatedBranchResponse.setStatus(false);
            foodicPaginatedBranchResponse.setMessage("Error occurred while trying to fetch foodics branches");
            return foodicPaginatedBranchResponse;
        }
    }
}

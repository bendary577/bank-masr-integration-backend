package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.Gson;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.GeneralSettings;
import com.sun.supplierpoc.models.aggregtor.BranchMapping;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminFailedResponse;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminToken;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatMenu;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.aggregtor.TalabatRest.RestOrder;
import com.sun.supplierpoc.models.aggregtor.branchAdmin.TalabatAdminOrder;
import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import com.sun.supplierpoc.models.configurations.AggregatorConfiguration;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TalabatAdminWebService {
    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    Logger logger = LoggerFactory.getLogger(CallRestService.class);
    /* Talabat branch base url */
    private static final String BASE_URL = "https://crs.me.restaurant-partners.com";

    /* Get talabat branch token */
    public TalabatAdminToken talabatLoginRequest(Account account, BranchMapping branchMapping) {

        TalabatAdminToken talabatAdminToken = new TalabatAdminToken();
        String endPoint = "/api/1/auth/form";
        try {

            AccountCredential credentials = account.getAccountCredentials().stream().filter(c -> c.getAccount().equals("Talabat")).
                    collect(Collectors.toSet()).stream().findAny().orElse(null);

            if (credentials == null) {
                talabatAdminToken.setStatus(false);
                talabatAdminToken.setMessage("No credentials found");
                return talabatAdminToken;
            }

            Gson gson = new Gson();
            String jsonCredential = gson.toJson(credentials);

            boolean talabatConnected = false;
            try {
                URL url = new URL(BASE_URL+endPoint);
                URLConnection connection = url.openConnection();
                connection.connect();
                talabatConnected = true;
            } catch (MalformedURLException e) {
//                System.out.println("Internet is not connected " + talabatConnected);
            } catch (IOException e) {
//                System.out.println("Internet is not connected " + talabatConnected);
            }

            if(talabatConnected){
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

                // Get Branch Credentials - e.g. password=123456&username=eg-holmes-san-stefano
                RequestBody body = RequestBody.create(mediaType, "password=" + branchMapping.getPassword() + "&username=" + branchMapping.getUsername());
//            RequestBody body = RequestBody.create(mediaType, "password=123456&username=eg-holmes-san-stefano");
                Request request = new Request.Builder()
                        .url(BASE_URL+endPoint)
                        .post(body)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .addHeader("cache-control", "no-cache")
                        .build();

                okhttp3.Response loginResponse = client.newCall(request).execute();

                gson = new Gson();

                talabatAdminToken = gson.fromJson(loginResponse.body().string(), TalabatAdminToken.class);

                talabatAdminToken.setStatus(loginResponse.code() == 200);
            }else{
                talabatAdminToken.setMessage("failed to connect to talabat webservice");
                talabatAdminToken.setStatus(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return talabatAdminToken;
    }

    public TalabatAdminOrder acceptService(TalabatAdminToken talabatAdminToken, RestOrder restOrder) {

        TalabatAdminOrder talabatAdminOrder = new TalabatAdminOrder();
        String endPoint = "/api/1/deliveries";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"state\":\"ACCEPTED\",\"deliveryTime\":10}");
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint + "/oma_" + restOrder.getIdentifier() + "/state")
                    .method("PUT", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization",
                            "Bearer " + talabatAdminToken.getToken()).build();

            okhttp3.Response getOrderRequest = client.newCall(request).execute();

            Gson gson = new Gson();

            talabatAdminOrder = gson.fromJson(getOrderRequest.body().string(), TalabatAdminOrder.class);
//            talabatAdminOrder = gson.fromJson("{\"id\":\"oma_" + restOrder.getIdentifier() + "\",\"timestamp\":\"2022-02-28T08:16:08.376Z\",\"state\":\"ACCEPTED\",\"dispatchStateType\":\"UNDEFINED\",\"trackingStateType\":\"NOT_TRACKED\",\"platformKey\":\"TB_OT\",\"globalEntityId\":\"HF_EG\",\"externalRestaurantId\":\"510703\",\"vendorName\":\"Test restaurant\",\"externalId\":\"TOTO-13a3411d-cecc-4dcb-9d2a-0c3bf8e72282\",\"test\":true,\"preorder\":false,\"guaranteed\":false,\"transport\":{\"type\":\"PICKUP_LOGISTICS\",\"transportName\":\"HURRIER\",\"driverId\":\"DE-65858\",\"driverName\":\"courier 84478\",\"pickupTime\":\"2022-02-28T08:26:08.566Z\"},\"seenAt\":\"2022-02-28T08:16:19.346Z\",\"deliverAt\":\"2022-02-28T08:36:08.566Z\",\"expiresAt\":\"2022-02-28T08:24:18.595Z\",\"promisedTime\":\"2022-02-28T08:31:07.895Z\",\"acceptedAt\":\"2022-02-28T08:16:53.777Z\",\"customer\":{\"customerId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"phone\":\"+49123456789\",\"firstName\":\"Max\"},\"address\":{\"customerAddressId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"street\":\"Oranienburger Straße 70\",\"zip\":\"10117\",\"city\":\"Berlin\",\"area\":\"Mitte\",\"block\":\"Block A\",\"floor\":\"1\",\"apartment\":\"42\",\"building\":\"67\",\"buildingName\":\"\",\"entrance\":\"Entrance on the left\",\"intercom\":\"1234\",\"info\":\"\",\"latitude\":52.524807,\"longitude\":13.392943,\"distance\":-1,\"geocodedManually\":false,\"formattedAddress\":\"\"},\"payment\":{\"paid\":false,\"currency\":\"EUR\",\"currencySymbol\":\"EGP\",\"total\":19.99,\"itemsTotalPrice\":3.95,\"paymentType\":\"CREDIT_CARD\",\"paymentMethod\":\"cash\",\"riderPaysAtRestaurant\":16.34,\"riderTip\":0,\"provideChangeFor\":0,\"currencyDecimals\":2},\"items\":[{\"amount\":1,\"name\":\"Pizza Salami\",\"category\":\"kfnm975\",\"menuNumber\":\"fjd-846\",\"comment\":\"the tomatoes should be fresh\",\"price\":3.95,\"total\":3.95,\"modifiers\":[{\"amount\":0,\"name\":\"large\",\"price\":0,\"total\":0,\"productId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"modifiable\":false,\"type\":\"VARIANT\"}],\"productId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"modifiable\":true,\"type\":\"PRODUCT\"}],\"taxes\":[{\"name\":\"VAT\",\"value\":4.26,\"includedInPrice\":true}],\"canVoid\":false,\"canDelay\":false,\"corporate\":false,\"shortCode\":\"456\",\"preparationCompleted\":false,\"preparationCompletionSupported\":true,\"accepter\":\"VENDOR\",\"vendorExtraParameters\":{},\"logisticsProviderId\":\"24816bc6-178b-4d68-b9bc-fbf13fd15f7c\",\"vendorTimeZone\":\"Africa/Cairo\",\"platformName\":\"Talabat\",\"acknowledged\":true,\"itemUnavailabilityHandling\":\"CONTACT_CUSTOMER\"}", TalabatAdminOrder.class);

            if (getOrderRequest.code() == 200) {
                talabatAdminOrder.setStatus(true);

            } else {
                talabatAdminOrder.setStatus(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return talabatAdminOrder;
    }

    /*
    * !!! Can not use it as we do not have order id
    * Get specific order details
    * Customer info, Items
    * */
    public TalabatAdminOrder getSingleOrderDetails(TalabatAdminToken talabatAdminToken, RestOrder restOrder) {

        TalabatAdminOrder talabatAdminOrder = new TalabatAdminOrder();
        String endPoint = "/api/1/deliveries";

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint + "/oma_" + restOrder.getIdentifier())
                    .method("GET", null)
                    .addHeader("Authorization",
                            "Bearer " + talabatAdminToken.getToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            if (orderResponse.code() == 200) {
                talabatAdminOrder = gson.fromJson(orderResponse.body().string(), TalabatAdminOrder.class);
                talabatAdminOrder.setStatus(true);
            } else {
                TalabatAdminFailedResponse failedResponse = gson.fromJson(orderResponse.body().string(), TalabatAdminFailedResponse.class);

                talabatAdminOrder.setStatus(false);
                talabatAdminOrder.setMessage(failedResponse.message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return talabatAdminOrder;
    }

    /*
     * Get all order details of last 1 min
     * Customer info, Items
     * */
    public TalabatAdminOrder[] getAllOrderDetails(TalabatAdminToken talabatAdminToken) {
        TalabatAdminOrder[] orders = new TalabatAdminOrder[0];
        String endPoint = "/api/2/deliveries";

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date currentDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.MINUTE, -1);
        Date oneMinBack = cal.getTime();

        Calendar calender = Calendar.getInstance();
        calender.setTime(currentDate);
        Date toDate = calender.getTime();

        String dailyParams = "?from=" + getDate() + "T00:00:00Z" + "&to=" + getDate() + "T23:59:59Z" +
//                "&statuses=ACCEPTED&statuses=PREORDER_ACCEPTED&statuses=WAITING_FOR_TRANSPORT" +
                "&statuses=CLOSED";
        String tempParams = "?from=2022-04-19T00:00:00Z&to=2022-04-21T" + dateFormat.format(toDate) + ":00Z";

        String dailyTesetParam = "?from=" + getDate() + "T" + dateFormat.format(oneMinBack) + ":00Z" +
                "&to=" + getDate() + "T" + dateFormat.format(toDate) + ":00Z";

        String TESTPARAM = "?from=2022-05-15T00:00:00Z&to=2022-05-15T23:59:59Z";

        String parameters = "?from=" + getDate() + "T" + dateFormat.format(oneMinBack) + ":00Z" +
                "&to=" + getDate() + "T" + dateFormat.format(toDate) + ":00Z" +
                "&statuses=ACCEPTED&statuses=PREORDER_ACCEPTED&statuses=NEW&statuses=WAITING_FOR_TRANSPORT";

        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint + parameters)
                    .method("GET", null)
                    .addHeader("Authorization",
                            "Bearer " + talabatAdminToken.getToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            if (orderResponse.code() == 200) {
                orders = gson.fromJson(orderResponse.body().string(), TalabatAdminOrder[].class);
            } else {
                TalabatAdminFailedResponse failedResponse = gson.fromJson(orderResponse.body().string(), TalabatAdminFailedResponse.class);
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return orders;
    }

    public TalabatAdminOrder updateOrderStatus(Account account, String talabatOrderID) {

        TalabatAdminOrder talabatAdminOrder = new TalabatAdminOrder();
        String endPoint = "/api/1/deliveries";
        TalabatAdminToken talabatAdminToken = new TalabatAdminToken();
        try {
            talabatAdminToken = talabatLoginRequest(account, new BranchMapping());
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint + talabatOrderID +  "/preparation-completion")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + talabatAdminToken.getToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            talabatAdminOrder = gson.fromJson(orderResponse.body().string(), TalabatAdminOrder.class);
//            talabatAdminOrder = gson.fromJson("{\"id\":\"oma_" + foodicsOrder.getId() +"\",\"timestamp\":\"2022-02-28T08:16:08.376Z\",\"state\":\"ACCEPTED\",\"dispatchStateType\":\"UNDEFINED\",\"trackingStateType\":\"NOT_TRACKED\",\"platformKey\":\"TB_OT\",\"globalEntityId\":\"HF_EG\",\"externalRestaurantId\":\"510703\",\"vendorName\":\"Test restaurant\",\"externalId\":\"TOTO-13a3411d-cecc-4dcb-9d2a-0c3bf8e72282\",\"test\":true,\"preorder\":false,\"guaranteed\":false,\"transport\":{\"type\":\"PICKUP_LOGISTICS\",\"transportName\":\"HURRIER\",\"driverId\":\"DE-65858\",\"driverName\":\"courier 84478\",\"pickupTime\":\"2022-02-28T08:26:08.566Z\"},\"seenAt\":\"2022-02-28T08:16:19.346Z\",\"deliverAt\":\"2022-02-28T08:36:08.566Z\",\"expiresAt\":\"2022-02-28T08:24:18.595Z\",\"promisedTime\":\"2022-02-28T08:31:07.895Z\",\"acceptedAt\":\"2022-02-28T08:16:53.777Z\",\"customer\":{\"customerId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"phone\":\"+49123456789\",\"firstName\":\"Max\"},\"address\":{\"customerAddressId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"street\":\"Oranienburger Straße 70\",\"zip\":\"10117\",\"city\":\"Berlin\",\"area\":\"Mitte\",\"block\":\"Block A\",\"floor\":\"1\",\"apartment\":\"42\",\"building\":\"67\",\"buildingName\":\"\",\"entrance\":\"Entrance on the left\",\"intercom\":\"1234\",\"info\":\"\",\"latitude\":52.524807,\"longitude\":13.392943,\"distance\":-1,\"geocodedManually\":false,\"formattedAddress\":\"\"},\"payment\":{\"paid\":false,\"currency\":\"EUR\",\"currencySymbol\":\"EGP\",\"total\":19.99,\"itemsTotalPrice\":3.95,\"paymentType\":\"CREDIT_CARD\",\"paymentMethod\":\"cash\",\"riderPaysAtRestaurant\":16.34,\"riderTip\":0,\"provideChangeFor\":0,\"currencyDecimals\":2},\"items\":[{\"amount\":1,\"name\":\"Pizza Salami\",\"category\":\"kfnm975\",\"menuNumber\":\"fjd-846\",\"comment\":\"the tomatoes should be fresh\",\"price\":3.95,\"total\":3.95,\"modifiers\":[{\"amount\":0,\"name\":\"large\",\"price\":0,\"total\":0,\"productId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"modifiable\":false,\"type\":\"VARIANT\"}],\"productId\":\"f8dc0072-234b-4c0b-b33f-124e9d209e46\",\"modifiable\":true,\"type\":\"PRODUCT\"}],\"taxes\":[{\"name\":\"VAT\",\"value\":4.26,\"includedInPrice\":true}],\"canVoid\":false,\"canDelay\":false,\"corporate\":false,\"shortCode\":\"456\",\"preparationCompleted\":false,\"preparationCompletionSupported\":true,\"accepter\":\"VENDOR\",\"vendorExtraParameters\":{},\"logisticsProviderId\":\"24816bc6-178b-4d68-b9bc-fbf13fd15f7c\",\"vendorTimeZone\":\"Africa/Cairo\",\"platformName\":\"Talabat\",\"acknowledged\":true,\"itemUnavailabilityHandling\":\"CONTACT_CUSTOMER\"}", TalabatAdminOrder.class);

            if (orderResponse.code() == 200) {
                talabatAdminOrder.setStatus(true);

            } else {
                talabatAdminOrder.setStatus(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return talabatAdminOrder;
    }

    public boolean readyForDeliveryOrder(Account account, String talabatOrderID, BranchMapping branch) {
        String endPoint = "/api/1/deliveries/";
        TalabatAdminToken talabatAdminToken = new TalabatAdminToken();
        try {
            talabatAdminToken = talabatLoginRequest(account, branch);
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(BASE_URL + endPoint + talabatOrderID +  "/preparation-completion")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + talabatAdminToken.getToken()).build();

            okhttp3.Response orderResponse = client.newCall(request).execute();

            Gson gson = new Gson();

            if (orderResponse.code() == 204) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }

        return false;
    }

    public TalabatMenu getTalabatBranchMenuItems(Account account, BranchMapping branchMapping) {
        TalabatMenu menu = new TalabatMenu();
        String endPoint = "/api/3/platforms/HF_EG/vendors/510703/menus?expand=false";
        TalabatAdminToken talabatAdminToken = new TalabatAdminToken();


        try {
            talabatAdminToken = talabatLoginRequest(account, branchMapping);

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
//                    .url("https://crs.me.restaurant-partners.com/api/3/platforms/HF_EG/vendors/510703/menus?expand=false")
                    .url(BASE_URL + endPoint)
                    .method("GET", null)
                    .addHeader("authorization", "Bearer " + talabatAdminToken.getToken())
                    .build();

            okhttp3.Response response = client.newCall(request).execute();

            Gson gson = new Gson();
            menu = gson.fromJson(response.body().string(), TalabatMenu.class);

            if (response.code() == 200) {
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            talabatAdminToken.setMessage(e.getMessage());
            talabatAdminToken.setStatus(false);
        }
        return menu;
    }


    public String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return sdf.format(date);
    }
}

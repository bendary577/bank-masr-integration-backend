package com.sun.supplierpoc.services.restTemplate;
import com.google.gson.Gson;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.OrderTypeChannels;
import com.sun.supplierpoc.models.configurations.SalesAPIConfig;
import com.sun.supplierpoc.models.configurations.SalesAPIStatistics;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class SyncSalesWebService {

    public Response syncSalesDailyAPI(SalesAPIStatistics salesAPIStatistics, SalesAPIConfig salesAPIConfig, List<HashMap<String, String>> responseData) {

        Response response = new Response();
        HashMap<String, String> salesAPIResponse  =new HashMap<>();
        String url = "https://apidev.emaar.com/etenantsales/dailysales";
        String requestBody = "";
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");

            String fandBSplit = getFundSplit(salesAPIStatistics);

            requestBody = "{\"SalesDataCollection\": " + "{\"SalesInfo\":[" +
                    "{\"UnitNo\":\"" + salesAPIStatistics.unitNo +"\"," + "\"LeaseCode\":\""+salesAPIStatistics.leaseCode+"\"," +
                    "\"SalesDate\": \""+salesAPIStatistics.dateFrom+"\"," + "\"TransactionCount\": "+salesAPIStatistics.NoChecks+"," +
                    "\"NetSales\": "+salesAPIStatistics.NetSales+"," + "\"FandBSplit\": [" + "{" + fandBSplit + "}]}]}}";

            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .addHeader("x-apikey", salesAPIConfig.apiKey)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "1ae7acda-de8d-b7e0-735a-eea92685be0f")
                    .build();
            okhttp3.Response salesResponse = client.newCall(request).execute();
            if (salesResponse.code() == 200){
                Gson gson = new Gson();

                salesAPIResponse = gson.fromJson(salesResponse.body().string(), HashMap.class);
                salesAPIResponse.put("storeName", salesAPIStatistics.getBrand());
                salesAPIResponse.put("storeNum", salesAPIStatistics.getUnitNo());
                salesAPIResponse.put("date", salesAPIStatistics.getDateFrom());
                salesAPIResponse.put("requestBody", requestBody);
                responseData.add(salesAPIResponse);
                response.setStatus(true);

            }else {
                response.setStatus(false);
                response.setMessage(salesResponse.message());
            }

        } catch (Exception e) {
            e.printStackTrace();
            salesAPIResponse.put("result: failed due to error ", e.getMessage());
            salesAPIResponse.put("storeName", salesAPIStatistics.getBrand());
            salesAPIResponse.put("storeNum", salesAPIStatistics.getUnitNo());
            salesAPIResponse.put("date", salesAPIStatistics.getDateFrom());
            salesAPIResponse.put("requestBody", requestBody);
            response.setData(salesAPIResponse);
            response.setStatus(false);
        }

        return response;
    }

    public Response syncSalesMonthlyAPI(SalesAPIStatistics salesAPIStatistics, SalesAPIConfig salesAPIConfig, List<HashMap<String, String>> responseData) {

        Response response = new Response();
        String requestBody = "";
        HashMap<String, Object> salesAPIResponse  =new HashMap<>();
        String url = "https://apidev.emaar.com/etenantsales/casualsales";
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");

            String fandBSplit = getFundSplit(salesAPIStatistics);
            requestBody = "{\"SalesDataCollection\": " + "{\"SalesInfo\": [" +
                            "{\"UnitNo\": \"" + salesAPIStatistics.unitNo +"\"," + "\"LeaseCode\": \""+salesAPIStatistics.leaseCode+"\"," +
                            "\"SalesDateFrom\": \""+salesAPIStatistics.dateFrom+"\"," + "\"SalesDateTo\": \""+salesAPIStatistics.dateTo+"\"," +
                            " \"TransactionCount\": "+salesAPIStatistics.NoChecks+"," + "\"TotalSales\": "+salesAPIStatistics.NetSales+"," +
                            "\"Remarks\": \"Remarks\"," + "\"FandBSplit\": [" + "{" + fandBSplit + "}]}]}}";

            RequestBody body = RequestBody.create(mediaType,requestBody);

            Request request = new Request.Builder().url(url).post(body)
                    .addHeader("content-type", "application/json")
                    .addHeader("x-apikey", salesAPIConfig.apiKey)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "1ae7acda-de8d-b7e0-735a-eea92685be0f")
                    .build();

            okhttp3.Response salesResponse = client.newCall(request).execute();
            if (salesResponse.code() == 200){
                Gson gson = new Gson();

                salesAPIResponse = gson.fromJson(salesResponse.body().string(), HashMap.class);
                salesAPIResponse.put("statistic", salesAPIStatistics);
                salesAPIResponse.put("RequestBody", requestBody);
                response.setData(salesAPIResponse);
                response.setStatus(true);

            }else {
                response.setStatus(false);
                response.setMessage(salesResponse.message());
            }

        } catch (Exception e) {
            e.printStackTrace();
            salesAPIResponse.put("statistic", salesAPIStatistics);
            salesAPIResponse.put("RequestBody", requestBody);
            response.setData(salesAPIResponse);
            response.setStatus(false);
        }

        return response;
    }


    private String getFundSplit(SalesAPIStatistics salesAPIStatistics) {

        String fandBSplit = "";
        for(OrderTypeChannels orderTypeChannels : salesAPIStatistics.orderTypeChannels){
            if(orderTypeChannels.isChecked()) {
                fandBSplit = fandBSplit + "\"" + orderTypeChannels.getChannel() + "\":" + orderTypeChannels.getNetSales() + ",";
            }
        }
        int index = 1 ;
        for(OrderTypeChannels orderTypeChannels : salesAPIStatistics.orderTypeChannels){
            if(orderTypeChannels.isChecked()) {
                if (index != salesAPIStatistics.orderTypeChannels.size()) {
                    fandBSplit = fandBSplit + "\"" + orderTypeChannels.getChannelCount() + "\":" + orderTypeChannels.getCheckCount() + ",";
                } else {
                    fandBSplit = fandBSplit + "\"" + orderTypeChannels.getChannelCount() + "\":" + orderTypeChannels.getCheckCount();
                }
            }
            index += 1;
        }
        return fandBSplit;
    }
}

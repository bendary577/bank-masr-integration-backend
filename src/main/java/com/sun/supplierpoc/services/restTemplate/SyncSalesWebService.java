package com.sun.supplierpoc.services.restTemplate;
import com.google.gson.Gson;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.OrderTypeChannels;
import com.sun.supplierpoc.models.configurations.SalesAPIConfig;
import com.sun.supplierpoc.models.configurations.SalesAPIStatistics;
import com.sun.supplierpoc.models.emaar.EmaarRoot;
import com.sun.supplierpoc.models.emaar.SalesDataCollection;
import com.sun.supplierpoc.models.emaar.SalesInfo;
import okhttp3.*;
import org.json.JSONObject;
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
/*            EmaarRoot root = new EmaarRoot();
            SalesDataCollection salesDataCollection = new SalesDataCollection();
            SalesInfo salesInfo = new SalesInfo();

            salesInfo.unitNo = salesAPIStatistics.unitNo;
            salesInfo.leaseCode = salesAPIStatistics.leaseCode;
            salesInfo.salesDate = salesAPIStatistics.dateFrom;
            salesInfo.transactionCount = salesAPIStatistics.NoChecks;
            salesInfo.netSales = salesAPIStatistics.NetSales;
            salesInfo.fandBSplit = "[{" + getFundSplit(salesAPIStatistics) + "}]";

            salesDataCollection.salesInfo.add(salesInfo);
            root.salesDataCollection = salesDataCollection;
            String body =new Gson().toJson(root);*/

            requestBody = "{\"SalesDataCollection\": " + "{\"SalesInfo\":[" +
                    "{\"UnitNo\":\"" + salesAPIStatistics.unitNo +"\"," + "\"LeaseCode\":\""+salesAPIStatistics.leaseCode+"\"," +
                    "\"SalesDate\": \""+salesAPIStatistics.dateFrom+"\"," + "\"TransactionCount\": "+salesAPIStatistics.NoChecks+"," +
                    "\"NetSales\": "+salesAPIStatistics.NetSales+"," + "\"FandBSplit\": [" + "{" + fandBSplit + "}]}]}}";

            response.setRequestbody(requestBody);

            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                    .url(salesAPIConfig.getApiURL()+salesAPIConfig.getApiEndpoint())
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .addHeader("x-apikey", salesAPIConfig.apiKey)
                    .addHeader("cache-control", "no-cache")
//                    .addHeader("postman-token", "1ae7acda-de8d-b7e0-735a-eea92685be0f")
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
                response.setData(responseData);

                if(salesAPIResponse.get("Code").equals("200")){
                    response.setStatus(true);
                }else {
                    response.setStatus(false);
                }
                response.setMessage(responseData.get(0).get("Result"));
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
        HashMap<String, String> salesAPIResponse  =new HashMap<>();
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

            Request request = new Request.Builder().url(salesAPIConfig.getApiURL()+salesAPIConfig.getApiEndpoint()).post(body)
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
                response.setData(responseData);
                response.setMessage(responseData.get(0).get("Result"));
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
            response.setMessage("Failed to send sales data to Emaar via API.");
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

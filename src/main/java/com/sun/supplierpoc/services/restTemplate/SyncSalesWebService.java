package com.sun.supplierpoc.services.restTemplate;
import com.google.gson.Gson;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.SalesAPIConfig;
import com.sun.supplierpoc.models.configurations.SalesAPIStatistics;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class SyncSalesWebService {

    public Response syncSalesDailyAPI(SalesAPIStatistics salesAPIStatistics, SalesAPIConfig salesAPIConfig) {

        Response response = new Response();
        String message = "";
        String url = "https://apidev.emaar.com/etenantsales/dailysales";
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,
                    "{\n  \"SalesDataCollection\": " +
                            "{\n    \"SalesInfo\": [\n " +
                            "     {\n        \"UnitNo\": \"" + salesAPIStatistics.unitNo +"\",\n " +
                            "       \"LeaseCode\": \""+salesAPIStatistics.leaseCode+"\",\n " +
                            "       \"SalesDate\": \""+salesAPIStatistics.dateFrom+"\",\n " +
                            "       \"TransactionCount\": "+salesAPIStatistics.NoChecks+",\n  " +
                            "      \"NetSales\": "+salesAPIStatistics.NetSales+"\n      }\n    ]\n  }\n}");
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

                HashMap salesAPI = gson.fromJson(salesResponse.body().string(),
                        HashMap.class);

                response.setStatus(true);
                response.setData(salesAPI);

            }else {
                message = salesResponse.message();
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

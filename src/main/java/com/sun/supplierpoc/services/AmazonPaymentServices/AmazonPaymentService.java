package com.sun.supplierpoc.services.AmazonPaymentServices;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

@Service
public class AmazonPaymentService {

    public void amazonPaymentService(AmazonPaymentServiceBody amazonPaymentServiceBody) throws IOException {

//        String url = "https://sbcheckout.PayFort.com/FortAPI/paymentPage";
        String url = "https://sbpaymentservices.payfort.com/FortAPI/paymentApi";

        String jsonRequestString =
                "{\"command\" : \"CAPTURE\" , \"access_code\" : \"zx0IPmPy5jp1vAz8Kpg7\"," +
                        " \"merchant_identifier\" : \"CycHZxVj\", " + "\"merchant_reference\" : \"XYZ9239-yu898\"," +
                        " \"amount\" : \"10000\", \"currency\" : \"AED\"," + "\"language\" : \"en\", \"fort_id\" : \"149295435400084008\", " +
                        "\"signature\" : \"7cad05f0212ed933c9a5d5dffa31661acf2c827a\", \"order_description\" : \"iPhone 6-S\"}";

        // Define and Initialize HttpClient
        HttpClient httpClient = HttpClientBuilder.create().build();

        // Initialize HttpPOST with FORT Payment services URL
        HttpPost request = new HttpPost(url);

        // Setup Http POST entity with JSON String
        StringEntity params = null;
        try {
            params = new StringEntity(jsonRequestString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Setup request type as JSON
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        // Post request to FORT
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read response using StringBuilder
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()), 65728);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        // Print response
        System.out.println(sb.toString());

    }

}

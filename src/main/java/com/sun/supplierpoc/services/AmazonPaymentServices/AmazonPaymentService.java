package com.sun.supplierpoc.services.AmazonPaymentServices;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.impl.client.HttpClients;

@Service
public class AmazonPaymentService {

    public void amazonPaymentService(String signature) throws IOException {

        String url = "https://sbcheckout.PayFort.com/FortAPI/paymentPage";
//        String url = "https://sbpaymentservices.payfort.com/FortAPI/paymentApi";

        String jsonRequestString =
                "{\"service_command\" : \"TOKENIZATION\" , \"access_code\" : \"zx0IPmPy5jp1vAz8Kpg7\"," +
                        "\"merchant_identifier\" : \"f0db228a\", " +
                        "\"merchant_reference\" : \"or1\"," +
                        "\"language\" : \"en\"," +
                        "\"expiry_date\" : \"05/21\", \"card_number\" : \"4005550000000001\"," +
                        "\"card_security_code\" : \"123\"," +
                        "\"signature\" : \""+ signature +"\"}";

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

    public void sendReq(String url, String email, String fname) {

        try {


            URL url1 = new URL("https://sbcheckout.PayFort.com/FortAPI/paymentPage");

            URLConnection connection = url1.openConnection();

            connection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());

            out.write("service_command=" + "TOKENIZATION" + "&access_code=" + "s31bpM1ebfNnwqo" + "&language=" + "en" +
                    "&access_code=" + "s31bpM1ebfNnwqo" + "&merchant_identifier=" + "FD1Ptq" + "&merchant_reference=" + "141127176" +
                    "&expiry_date=" + "05/21" + "&card_number=" + "4005550000000001" + "&card_security_code=" + "123" +
                    "&signature=" + "0E14E20BF3827A18566117958A4EE40C9AF470EDC57359B3933CB4631811BCF5" +
                    "&return_url=" + "http://localhost:8081/activity/test   ");


            // remember to clean up
            out.flush();
            out.close();
        }catch (Exception e){
            LoggerFactory.getLogger(AmazonPaymentService.class).info(e.getMessage());
        }
    }

    public String getSignature() {

        Map<String, Object> requestMap = new HashedMap();
        requestMap.put("service_command ", "TOKENIZATION");
        requestMap.put("access_code", "zx0IPmPy5jp1vAz");
        requestMap.put("merchant_identifier", "f0db228a");
        requestMap.put("merchant_reference", "or1");
        requestMap.put("language", "ar");

//        Map<String, Object> requestMap = new HashedMap();
//        requestMap.put("service_command ", "AUTHORIZATION");
//        requestMap.put("access_code", "zx0IPmPy5jp1vAz");
//        requestMap.put("merchant_identifier", "f0db228a");
//        requestMap.put("merchant_reference", "or1");
//        requestMap.put("amount", "10000");
//        requestMap.put("currency", "EGP");
//        requestMap.put("language", "ar");
//        requestMap.put("customer_email", "bassel759@yahoo.com");
//        requestMap.put("order_description", "iPhone 6-S");

//        {\"command\" : \"CAPTURE\" ," +
//        \"access_code\" : \"zx0IPmPy5jp1vAz8Kpg7\"," +
//                " \"merchant_identifier\" : \"CycHZxVj\", " +
//                "\"merchant_reference\" : \"XYZ9239-yu898\"," +
//                " \"amount\" : \"10000\"," +
//                " \"currency\" : \"AED\"," +
//                "\"language\" : \"en\"," +
//                " \"fort_id\" : \"149295435400084008\", " +
//                " \"order_description\" : \"iPhone 6-S\"}
        try {

            //order by key
            requestMap = requestMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            String requestString = "PASS";

            for (Map.Entry<String, Object> entry : requestMap.entrySet())
                requestString += entry.getKey() + "=" + entry.getValue();
            requestString += "PASS";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(requestString.getBytes(StandardCharsets.UTF_8));
            String signature = javax.xml.bind.DatatypeConverter.printHexBinary(hashed);
            LoggerFactory.getLogger(AmazonPaymentService.class).info(signature);
            return signature;
        } catch (Exception e) {
            return "";

        }
    }
}

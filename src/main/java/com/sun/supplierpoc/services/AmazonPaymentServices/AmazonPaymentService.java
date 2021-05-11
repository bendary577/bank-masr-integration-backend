package com.sun.supplierpoc.services.AmazonPaymentServices;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import okhttp3.*;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AmazonPaymentService {

    public String getSignature(HashedMap requestSignature) {

        Map<String, Object> requestMap = requestSignature;

        try {

            //order by key
            requestMap = requestMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            String requestString = Constants.SIGNATURE_PHRASE;

            for (Map.Entry<String, Object> entry : requestMap.entrySet())
                requestString += entry.getKey() + "=" + entry.getValue();
            requestString += Constants.SIGNATURE_PHRASE;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(requestString.getBytes(StandardCharsets.UTF_8));
            String signature = javax.xml.bind.DatatypeConverter.printHexBinary(hashed);
            LoggerFactory.getLogger(AmazonPaymentService.class).info(signature);
            return signature;

        } catch (Exception e) {

            LoggerFactory.getLogger(AmazonPaymentService.class).info(e.getMessage());
            throw new RuntimeException("Can't build signature!.");

        }
    }

    public void amazonPaymentSendTokenization(String signature) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
                "service_command=TOKENIZATION&" +
                        "access_code=Y6lL5f0wvaKSxdM8jsjr&" +
                        "merchant_identifier=f0db228a&" +
                        "merchant_reference=OR113&" +
                        "language=en&expiry_date=2105&" +
                        "card_number=4005550000000001&" +
                        "card_security_code=123&" +
                        "signature=2C6FE3370926DC8145E6E8966A13AA17EDF23DF1FC61F3556CFFC30231737352&" +
                        "return_url=http://localhost:8081/amazon/acceptRequest");

        Request request = new Request.Builder()
                .url("https://sbcheckout.PayFort.com/FortAPI/paymentPage")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", "JSESSIONID=-uiWCUJFPPy4nUXpuay2kbhoPnct4bPWOI6aaeNs.ip-10-50-212-94; __cfduid=d5d92041f141dc35b692d20d41ef9215c1617632522")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());

    }


    public void amazonPaymentService(AmazonPaymentServiceBody amazonPaymentServiceBody) throws IOException {

//        String url = "https://sbcheckout.PayFort.com/FortAPI/paymentPage";
        String url = "https://sbpaymentservices.payfort.com/FortAPI/paymentApi";

        String jsonRequestString =
                "{\"service_command\" : \"TOKENIZATION\" , \"access_code\" : \"Y6lL5f0wvakSxdM8jsjr\"," +
                        " \"merchant_identifier\" : \"f0db228a\", " +
                        "\"merchant_reference\" : \"or100\"," +
                        " \"amount\" : \"10000\", \"currency\" : \"EGY\"," +
                        "\"language\" : \"en\", \"fort_id\" : \"149295435400084008\", " +
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

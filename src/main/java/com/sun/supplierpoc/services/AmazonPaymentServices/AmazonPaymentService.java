package com.sun.supplierpoc.services.AmazonPaymentServices;

import com.sun.supplierpoc.models.amazonPayment.AmazonPaymentServiceBody;
import com.sun.supplierpoc.models.opera.TransactionRequest;
import com.sun.supplierpoc.models.opera.TransactionResponse;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AmazonPaymentService {

    public String amazonPaymentSendTokenization(String signature, String cardNumber, String cardSecurityCode,
                                              String orderId) throws IOException {

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost url = new HttpPost("https://sbcheckout.PayFort.com/FortAPI/paymentPage");
        String tokenName = "";

        ArrayList<NameValuePair> params = new ArrayList();
        params.add(new BasicNameValuePair("service_command", "TOKENIZATION"));
        params.add(new BasicNameValuePair("access_code", "Y6lL5f0wvaKSxdM8jsjr"));
        params.add(new BasicNameValuePair("merchant_identifier", "f0db228a"));
        params.add(new BasicNameValuePair("merchant_reference", orderId));
        params.add(new BasicNameValuePair("language", "en"));
        params.add(new BasicNameValuePair("expiry_date", "2105"));
        params.add(new BasicNameValuePair("card_number", cardNumber));
        params.add(new BasicNameValuePair("card_security_code", cardSecurityCode));
        params.add(new BasicNameValuePair("signature", signature));

        url.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(url);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                // do something useful
            }
        }
        return tokenName;
    }

    public TransactionResponse amazonPaymentService(TransactionRequest transactionRequest){
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setSequenceNo(transactionRequest.getSequenceNo());
        transactionResponse.setTransType(transactionRequest.getTransType());
        transactionResponse.setTransAmount(transactionRequest.getTransAmount());

        transactionResponse.setRespCode("00");
        transactionResponse.setRespText("APPROVAL");
        transactionResponse.setpAN("XXXXXXXXXXXXXX2811");
        transactionResponse.setExpiryDate("2212");
        transactionResponse.setTransToken("131111111111112811");
        transactionResponse.setEntryMode("01");
        transactionResponse.setIssuerId("01");
        transactionResponse.setrRN("000000000311");
        transactionResponse.setOfflineFlag("N");
        transactionResponse.setMerchantId("1");
        transactionResponse.setdCCIndicator("0");
        transactionResponse.setTerminalId("1");
        return transactionResponse;
    }

    public void amazonPaymentTokenization(AmazonPaymentServiceBody amazonPaymentServiceBody) throws IOException {

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


    public String getSignature(HashedMap c){

        Map<String, Object> requestMap = c;

//        requestMap.put("command", "AUTHORIZATION");
//        requestMap.put("access_code", "Y6lL5f0wvaKSxdM8jsjr");
//        requestMap.put("merchant_identifier", "f0db228a");
//        requestMap.put("merchant_reference", "or102251");
//        requestMap.put("amount", "100000");
//        requestMap.put("currency", "SAR");
//        requestMap.put("language", "en");
//        requestMap.put("customer_email", "basel@yahoo.com");
//        requestMap.put("customer_ip", "2001:0db8:3042:0002:5a55:caff:fef6:bdbf");
//        requestMap.put("token_name", "749ee1fbe97049a69cfe6926b39372c3");

//        requestMap.put("service_command", "TOKENIZATION");
//        requestMap.put("access_code", "Y6lL5f0wvaKSxdM8jsjr");
//        requestMap.put("merchant_identifier", "f0db228a");
//        requestMap.put("merchant_reference", "or102251");
//        requestMap.put("language", "en");
//        requestMap.put("return_url", "http://localhost:8081/amazon/acceptRequest");

        try {

            //order by key
            requestMap = requestMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            String requestString = "68D2fyokjF9UCt2x45V7SD(@";

            for (Map.Entry<String, Object> entry : requestMap.entrySet())
                requestString += entry.getKey() + "=" + entry.getValue();
            requestString += "68D2fyokjF9UCt2x45V7SD(@";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(requestString.getBytes(StandardCharsets.UTF_8));
            String signature = javax.xml.bind.DatatypeConverter.printHexBinary(hashed);
            LoggerFactory.getLogger(AmazonPaymentService.class).info(signature);
            return signature;
        }catch(Exception e){
            return "";
        }
    }
}

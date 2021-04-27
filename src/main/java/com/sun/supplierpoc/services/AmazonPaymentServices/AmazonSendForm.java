package com.sun.supplierpoc.services.AmazonPaymentServices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class AmazonSendForm {

    public void amazonPaymentSendTokenization(String signature) throws IOException {

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://sbcheckout.PayFort.com/FortAPI/paymentPage");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(9);
        params.add(new BasicNameValuePair("service_command", "TOKENIZATION"));
        params.add(new BasicNameValuePair("access_code", "Y6lL5f0wvaKSxdM8jsjr"));
        params.add(new BasicNameValuePair("merchant_identifier", "f0db228a"));
        params.add(new BasicNameValuePair("merchant_reference", "or102250"));
        params.add(new BasicNameValuePair("language", "en"));
        params.add(new BasicNameValuePair("expiry_date", "2105"));
        params.add(new BasicNameValuePair("card_number", "4005550000000001"));
        params.add(new BasicNameValuePair("card_security_code", "123"));
        params.add(new BasicNameValuePair("signature", signature));
        params.add(new BasicNameValuePair("return_url", "http://localhost:8081/amazon/acceptRequest"));

        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                // do something useful
            }
        }

    }
}
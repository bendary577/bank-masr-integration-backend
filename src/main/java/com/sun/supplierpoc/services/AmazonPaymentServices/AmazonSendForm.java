package com.sun.supplierpoc.services.AmazonPaymentServices;

import okhttp3.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class AmazonSendForm {

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

}

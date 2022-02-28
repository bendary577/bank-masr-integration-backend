package com.sun.supplierpoc.services.restTemplate;

import com.google.gson.Gson;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.talabat.branchAdmin.Token;
import com.sun.supplierpoc.services.simphony.CallRestService;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TalabatAdminWebService {

    Logger logger = LoggerFactory.getLogger(CallRestService.class);
    private static final String BASE_URL = "https://crs.me.restaurant-partners.com/api/1/";

    public Token talabatLoginRequest(Account account) {

        Token token = new Token();
        String endPoint = "auth/formn";
        try {

            AccountCredential credentials = account.getAccountCredentials().stream().filter(c -> c.getAccount().equals("Talabat")).collect(Collectors.toSet()).stream().findAny().orElse(null);

            if (credentials == null) {
                token.setStatus(false);
                token.setMessage("No credentials found");
                return token;
            }

            Gson gson = new Gson();
            String jsonCredential = gson.toJson(credentials);

            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "password=123456&username=eg-holmes-san-stefano");
            Request request = new Request.Builder()
                    .url("https://crs.me.restaurant-partners.com/api/1/auth/form")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "b392ce40-9136-ed32-f6cd-1c3d87cd98b8")
                    .build();

            okhttp3.Response loginResponse = client.newCall(request).execute();

            gson = new Gson();

            token = gson.fromJson(loginResponse.body().string(), Token.class);

            token.setStatus(loginResponse.code() == 200);

        } catch (Exception e) {
            e.printStackTrace();
            token.setMessage(e.getMessage());
            token.setStatus(false);
        }

        return token;
    }


}

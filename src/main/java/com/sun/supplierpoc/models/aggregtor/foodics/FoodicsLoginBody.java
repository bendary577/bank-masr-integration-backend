package com.sun.supplierpoc.models.aggregtor.foodics;

public class FoodicsLoginBody {

    private String grantType = "password";
    private String code = "def50200a27ea582fbb116012318b65169f485e2c1eb64daXXXXXXX";
    private String clientId = "9042bfae-71d2-400f-b45e-b45e3282b45e";
    private String clientSecret = "ab45eUx4FHb45eqjb45eUuMGdEqUNI0Z9b45eUx4Fb45eUx4F";
    private String redirectUri = "https://www.talabat.com/foodics/oauth2/callback";
    private String tokenType= "";
    private String accessToken = "";
    private boolean status;
    private String message;

    private String getGrantType() {
        return grantType;
    }

    private void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    private String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    private String getClientId() {
        return clientId;
    }

    private void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private String getClientSecret() {
        return clientSecret;
    }

    private void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    private String getRedirectUri() {
        return redirectUri;
    }

    private void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

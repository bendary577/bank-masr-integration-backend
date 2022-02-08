package com.sun.supplierpoc.models.talabat.login;

public class Token {

    public String refreshToken;
    public String tokenType;
    public User user;
    public String role;
    public String accessToken;
    public AccessTokenContent accessTokenContent;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public AccessTokenContent getAccessTokenContent() {
        return accessTokenContent;
    }

    public void setAccessTokenContent(AccessTokenContent accessTokenContent) {
        this.accessTokenContent = accessTokenContent;
    }
}

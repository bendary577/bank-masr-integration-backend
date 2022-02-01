package com.sun.supplierpoc.models.talabat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Token {

    public String refreshToken;
    public String tokenType;
    public User user;
    public String role;
    public String accessToken;
    public AccessTokenContent accessTokenContent;

    public class User{
        public Date createdAt;
        public String email;
        public String locale;
        public String name;
        public String role;

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public class AccessTokenContent{
        public String country;
        public User user;
        public String version;
        public boolean impersonator;
        public HashMap vendors;
        public int iat;
        public int exp;
        public String aud;
        public String iss;
        public String sub;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public boolean isImpersonator() {
            return impersonator;
        }

        public void setImpersonator(boolean impersonator) {
            this.impersonator = impersonator;
        }

        public HashMap getVendors() {
            return vendors;
        }

        public void setVendors(HashMap vendors) {
            this.vendors = vendors;
        }

        public int getIat() {
            return iat;
        }

        public void setIat(int iat) {
            this.iat = iat;
        }

        public int getExp() {
            return exp;
        }

        public void setExp(int exp) {
            this.exp = exp;
        }

        public String getAud() {
            return aud;
        }

        public void setAud(String aud) {
            this.aud = aud;
        }

        public String getIss() {
            return iss;
        }

        public void setIss(String iss) {
            this.iss = iss;
        }

        public String getSub() {
            return sub;
        }

        public void setSub(String sub) {
            this.sub = sub;
        }
    }

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

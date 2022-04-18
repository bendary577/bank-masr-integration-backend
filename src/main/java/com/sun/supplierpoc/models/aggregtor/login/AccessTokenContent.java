package com.sun.supplierpoc.models.aggregtor.login;

import java.util.HashMap;

public class AccessTokenContent {
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

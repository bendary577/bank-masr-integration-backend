package com.sun.supplierpoc.models.auth;

import com.sun.supplierpoc.converters.SerializableObjectConverter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.provider.OAuth2Authentication;


@Document(collection = "oauthAuthorizationCodes")
public class AuthorizationCode {

    public static final String CODE = "code";

    @Id
    private String id;

    private String code;
    private String authentication;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public OAuth2Authentication getAuthentication() {
        return SerializableObjectConverter.deserialize(authentication);
    }

    public void setAuthentication(OAuth2Authentication authentication) {
        this.authentication = SerializableObjectConverter.serialize(authentication);;
    }
}

package com.sun.supplierpoc.services.security;


import com.sun.supplierpoc.models.auth.AuthorizationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

/**
 * Created by jeebb on 11/10/14.
 */
public class CustomAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setCode(code);
        authorizationCode.setAuthentication(authentication);
        mongoTemplate.save(authorizationCode);
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        Query query = new Query();
        query.addCriteria(Criteria.where(AuthorizationCode.CODE).is(code));
        OAuth2Authentication authentication = null;
        AuthorizationCode authorizationCode = mongoTemplate.findOne(query, AuthorizationCode.class);
        if (authorizationCode != null) {
            authentication = authorizationCode.getAuthentication();
        }
        return authentication;
    }
}

package com.sun.supplierpoc.services.security;


import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.sun.supplierpoc.models.auth.OauthClientDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.provider.*;

import java.util.ArrayList;
import java.util.List;

public class CustomClientDetailsService implements ClientDetailsService, ClientRegistrationService {

    @Autowired
    private MongoTemplate  mongoTemplate;

    @Override
    public org.springframework.security.oauth2.provider.ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Query query = new Query();
        query.addCriteria(Criteria.where(OauthClientDetails.CLIENT_ID).is(clientId));
        OauthClientDetails oauthClientDetails = mongoTemplate.findOne(query, OauthClientDetails.class);
/*        if (oauthClientDetails == null) {
            throw new ClientRegistrationException(String.format("Client with id %s not found", clientId));
        }*/
        return oauthClientDetails;
    }

    @Override
    public void addClientDetails(org.springframework.security.oauth2.provider.ClientDetails clientDetails) throws ClientAlreadyExistsException {
        if (loadClientByClientId(clientDetails.getClientId()) == null) {
            OauthClientDetails mongoOauthClientDetails =
                    new OauthClientDetails(clientDetails.getClientId(), clientDetails.getResourceIds(),
                            clientDetails.isSecretRequired(),clientDetails.getClientSecret(),
                            clientDetails.isScoped(),
                            clientDetails.getScope(), clientDetails.getAuthorizedGrantTypes(), clientDetails.getRegisteredRedirectUri(),
                            clientDetails.getAuthorities(), clientDetails.getAccessTokenValiditySeconds(),
                            clientDetails.getRefreshTokenValiditySeconds(), clientDetails.isAutoApprove("true"),
                            clientDetails.getAdditionalInformation());
            mongoTemplate.save(mongoOauthClientDetails);
        } else {
            throw new ClientAlreadyExistsException(String.format("Client with id %s already existed",
                    clientDetails.getClientId()));
        }
    }

    @Override
    public void updateClientDetails(org.springframework.security.oauth2.provider.ClientDetails clientDetails) throws NoSuchClientException {
        Query query = new Query();
        query.addCriteria(Criteria.where(OauthClientDetails.CLIENT_ID).is(clientDetails.getClientId()));

        Update update = new Update();
        update.set(OauthClientDetails.RESOURCE_IDS, clientDetails.getResourceIds());
        update.set(OauthClientDetails.SCOPE, clientDetails.getScope());
        update.set(OauthClientDetails.AUTHORIZED_GRANT_TYPES, clientDetails.getAuthorizedGrantTypes());
        update.set(OauthClientDetails.REGISTERED_REDIRECT_URI, clientDetails.getRegisteredRedirectUri());
        update.set(OauthClientDetails.AUTHORITIES, clientDetails.getAuthorities());
        update.set(OauthClientDetails.ACCESS_TOKEN_VALIDITY_SECONDS, clientDetails.getAccessTokenValiditySeconds());
        update.set(OauthClientDetails.REFRESH_TOKEN_VALIDITY_SECONDS, clientDetails.getRefreshTokenValiditySeconds());
        update.set(OauthClientDetails.ADDITIONAL_INFORMATION, clientDetails.getAdditionalInformation());

        UpdateResult writeResult = mongoTemplate.updateFirst(query, update, OauthClientDetails.class);

        if(writeResult.getMatchedCount() <= 0) {
            throw new NoSuchClientException(String.format("Client with id %s not found", clientDetails.getClientId()));
        }
    }

    @Override
    public void updateClientSecret(String clientId, String clientSecret) throws NoSuchClientException {
        Query query = new Query();
        query.addCriteria(Criteria.where(OauthClientDetails.CLIENT_ID).is(clientId));

        Update update = new Update();
        update.set(OauthClientDetails.CLIENT_SECRET, clientSecret);

        UpdateResult writeResult = mongoTemplate.updateFirst(query, update, OauthClientDetails.class);

        if(writeResult.getMatchedCount() <= 0) {
            throw new NoSuchClientException(String.format("Client with id %s not found", clientId));
        }
    }

    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {
        Query query = new Query();
        query.addCriteria(Criteria.where(OauthClientDetails.CLIENT_ID).is(clientId));

        DeleteResult writeResult = mongoTemplate.remove(query, OauthClientDetails.class);

        if(writeResult.getDeletedCount() <= 0) {
            throw new NoSuchClientException(String.format("Client with id %s not found", clientId));
        }
    }

    @Override
    public List<org.springframework.security.oauth2.provider.ClientDetails> listClientDetails() {
        List<org.springframework.security.oauth2.provider.ClientDetails> result =  new ArrayList<org.springframework.security.oauth2.provider.ClientDetails>();
        List<OauthClientDetails> details = mongoTemplate.findAll(OauthClientDetails.class);
        for (OauthClientDetails detail : details) {
            result.add(detail);
        }
        return result;
    }

}
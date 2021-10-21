package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.lang.constant.Constable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;

    public Feature save(Feature feature) {
        if(featureRepository.existsByName(feature.getName()))
            return new Feature();
        Feature tempFeature;
        try {
            tempFeature = featureRepository.save(feature);
        }catch (Exception e){
            tempFeature = new Feature();
        }
        return tempFeature;
    }

    public List<Feature> findAllFeature(String accountId) {
        List<Feature> features ;
        if(accountId.equals("")){
            features = featureRepository.findAll();
        }else{
            Account account = accountService.getAccount(accountId);
            features = account.getFeatures();
        }
        return features;
    }

    public Response setAccountFeatures(String accountId, List<String> featuresIds) {

        Response response = new Response();

        Optional<Account> accountOptional = accountService.getAccountOptional(accountId);
        List<Feature> features = new ArrayList<>() ;

        if(accountOptional.isPresent()){
            Account account = accountOptional.get();
            for(String featureId : featuresIds){
                Optional<Feature> featureOptional = featureRepository.findById(featureId);
                if(!featureOptional.isPresent()){
                    response.setStatus(false);
                    response.setMessage(Constants.INVALID_FEATURE_ID + featureId);
                    return response;
                }
                features.add(featureOptional.get());
            }

            account.getFeatures().addAll(features);
            accountRepo.save(account);

            response.setStatus(true);
            response.setData(account);
            return response;

        }else{
            response.setStatus(false);
            response.setMessage(Constants.ACCOUNT_NOT_EXIST);
            return response;
        }

    }

    public boolean hasFeature(Account account, String featureRef) {
        List<Feature> features = account.getFeatures();
        boolean hasRole = false;
        for (Feature f : features) {
            if(f.getReference().toLowerCase().equals(featureRef.toLowerCase())){
                hasRole = true; break;
            }
        }
        return hasRole;
    }
}

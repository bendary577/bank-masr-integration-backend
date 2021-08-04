package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private AccountService accountService;

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

            features = featureRepository.findAllByIdIn(account.getFeatureIds());
        }

        return features;

    }
}

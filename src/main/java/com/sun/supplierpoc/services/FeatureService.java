package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.repositories.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;


    public Feature save(Feature feature) {

        Feature tempFeature;
        try {
            tempFeature = featureRepository.save(feature);
        }catch (Exception e){
            tempFeature = new Feature();
        }

        return tempFeature;
    }
}

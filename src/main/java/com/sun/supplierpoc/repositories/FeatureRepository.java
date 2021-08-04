package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Feature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureRepository extends MongoRepository<Feature, String> {

    boolean existsByName(String name);

    List<Feature> findAllByIdIn(List<String> featureIds);
}

package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Feature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends MongoRepository<Feature, String> {
}

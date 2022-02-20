package com.sun.supplierpoc.repositories.applications;

import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodicsProductRepo extends MongoRepository<Product, String> {
}

package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.talabat.foodics.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodicsProductRepo extends MongoRepository<Product, String> {
}

package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}

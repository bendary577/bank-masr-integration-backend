package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.aggregtor.foodics.FoodicsOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodicsOrderRepo  extends MongoRepository<FoodicsOrder, String> {
}

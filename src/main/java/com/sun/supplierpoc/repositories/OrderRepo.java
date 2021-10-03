package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository

public interface OrderRepo extends MongoRepository<Order, String>{
    Optional<Order> findByIdAndDeleted(String orderId, boolean deleted);
    List<Order> findAll();
}

package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AggregatorOrder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository

public interface OrderRepo extends MongoRepository<AggregatorOrder, String>{

    Optional<AggregatorOrder> findByIdAndDeleted(String orderId, boolean deleted);

    List<AggregatorOrder> findAll();

    List<AggregatorOrder> findAllByAccountOrderByCreationDateDesc(Account account);

    Optional<AggregatorOrder> findByFoodicsOrderId(String id);
}

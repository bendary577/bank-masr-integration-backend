package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.AggregatorOrder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository

public interface OrderRepo extends MongoRepository<AggregatorOrder, String>{

    Optional<AggregatorOrder> findByIdAndDeleted(String orderId, boolean deleted);

    List<AggregatorOrder> findAll();

    List<AggregatorOrder> findAllByAccountOrderByCreationDateDesc(Account account, Pageable pageable);

    Optional<AggregatorOrder> findByFoodicsOrderId(String id);
    int countAllByAccountId(String id);

    @Query("{'date' : { $gte: ?0, $lte: ?1 } }")
    List<AggregatorOrder> getAllBetweenDates(Date startDate, Date endDate);

}

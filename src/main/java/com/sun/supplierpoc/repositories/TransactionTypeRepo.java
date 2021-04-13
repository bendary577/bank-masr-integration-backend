package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepo extends MongoRepository<TransactionType, String> {

    TransactionType findByNameAndAccountId(String name, String accountId);

}

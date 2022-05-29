package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.TransactionType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionTypeRepo extends MongoRepository<TransactionType, String> {

    TransactionType findByNameAndAccountId(String name, String accountId);

    TransactionType findByIdAndAccountId(String transactionTypeId, String id);

    List<TransactionType> findByAccountIdAndDeleted(String id, boolean deleted);

    List<TransactionType> findByAccountId(String id);
}

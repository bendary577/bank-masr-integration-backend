package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Transactions;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepo extends MongoRepository<Transactions, String> {

    List<Transactions> findAllByTransactionTypeId(String transactionType);

}

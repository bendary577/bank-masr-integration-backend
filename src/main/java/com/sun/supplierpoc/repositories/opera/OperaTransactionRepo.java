package com.sun.supplierpoc.repositories.opera;

import com.sun.supplierpoc.models.OperaTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperaTransactionRepo extends MongoRepository<OperaTransaction, String> {
    List<OperaTransaction> findAllByAccountIdAndDeleted(String accountId, boolean deleted);
}

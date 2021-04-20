package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepo extends MongoRepository<Transactions, String> {

    List<Transactions> findAllByTransactionTypeId(String transactionType);
    List<Transactions> findAllByTransactionTypeIdAndTransactionDateBetween(String accountI,Date from, Date to);

    List<Transactions> findAllByUser(ApplicationUser user);

    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);

    boolean existsByCheckNumber(String checkNumber);
}

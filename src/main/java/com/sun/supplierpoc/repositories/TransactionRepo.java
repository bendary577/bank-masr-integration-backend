package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepo extends MongoRepository<Transactions, String> {

    List<Transactions> findAllByTransactionTypeIdOrderByTransactionDateDesc(String transactionType);

    List<Transactions> findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(String accountI,Date from, Date to);

    List<Transactions> findAllByGroupAndTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(Group group, String accountI, Date from, Date to);

    List<Transactions> findAllByGroupAndTransactionTypeIdOrderByTransactionDateDesc(Group group, String accountI);

    List<Transactions> findAllByUserOrderByTransactionDateDesc(ApplicationUser user);

    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);

    boolean existsByCheckNumberAndRevenueCentreId(String checkNumber, int revenueCentreId);

//    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);
}

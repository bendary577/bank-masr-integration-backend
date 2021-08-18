package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface TransactionRepo extends MongoRepository<Transactions, String> {

    List<Transactions> findAllByTransactionTypeIdOrderByTransactionDateDesc(String transactionType);

    List<Transactions> findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(String accountI,Date from, Date to);

    List<Transactions> findAllByGroupIdAndTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(String groupId, String accountI, Date from, Date to);

    List<Transactions> findAllByGroupAndTransactionTypeIdOrderByTransactionDateDesc(Group group, String accountI);

    List<Transactions> findAllByUserOrderByTransactionDateDesc(ApplicationUser user);

    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);

    boolean existsByCheckNumberAndRevenueCentreId(String checkNumber, int revenueCentreId);

    List<Transactions> findAllByGroupIdAndTransactionTypeIdOrderByTransactionDateDesc(String groupId, String id);

    List<Transactions> findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Date start, Date end);

    List<Transactions> findAllByGroupIdAndTransactionTypeInOrderByTransactionDateDesc(String id, List<TransactionType> transactionTypes);

    List<Transactions> findAllByTransactionTypeInOrderByTransactionDateDesc(List<TransactionType> transactionTypes);

    List<Transactions> findAllByGroupIdAndTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(String id, List<TransactionType> transactionTypes, Date start, Date end);

//    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);
}

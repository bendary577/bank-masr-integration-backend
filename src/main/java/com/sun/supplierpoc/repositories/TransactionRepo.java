package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.TransactionType;
import com.sun.supplierpoc.models.Transactions;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface TransactionRepo extends MongoRepository<Transactions, String> {
    /*
    * Find transactions by transaction type
    * */

    List<Transactions> findAllByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionTypes);
    List<Transactions> findAllByTransactionTypeAndGroupOrderByTransactionDateDesc(TransactionType transactionTypes, Group group);
    List<Transactions> findAllByTransactionTypeOrderByTransactionDateDesc(TransactionType transactionTypes, Pageable pageable);
    List<Transactions> findAllByTransactionTypeAndGroupOrderByTransactionDateDesc(TransactionType transactionTypes, Group group, Pageable pageable);

    List<Transactions> findAllByTransactionTypeAndTransactionDateBetweenOrderByTransactionDateDesc(TransactionType transactionTypes, Date start, Date end);
    List<Transactions> findAllByTransactionTypeAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(TransactionType transactionTypes, Group group, Date start, Date end);
    List<Transactions> findAllByTransactionTypeAndTransactionDateBetweenOrderByTransactionDateDesc(TransactionType transactionTypes, Date start, Date end, Pageable pageable);
    List<Transactions> findAllByTransactionTypeAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(TransactionType transactionTypes, Group group, Date start, Date end, Pageable pageable);

    /*
     * Filter transactions by type, date and group
     * */
    int countAllByTransactionTypeIn(List<TransactionType> transactionTypes);
    int countAllByTransactionTypeInAndTransactionDateBetween(List<TransactionType> transactionTypes, Date start, Date end);

    // Add group
    int countAllByTransactionTypeIdAndGroup(String transactionTypes, Group group);
    int countAllByTransactionTypeInAndTransactionDateBetween(List<TransactionType> transactionTypes,Group groupId, Date start, Date end);

    /*
    *
    * */
    List<Transactions> findAllByTransactionTypeIdOrderByTransactionDateDesc(String transactionType);

    List<Transactions> findAllByTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(String accountI,Date from, Date to);

    List<Transactions> findAllByGroupIdAndTransactionTypeIdAndTransactionDateBetweenOrderByTransactionDateDesc(String groupId, String accountI, Date from, Date to);

    List<Transactions> findAllByGroupAndTransactionTypeIdOrderByTransactionDateDesc(Group group, String accountI);

    List<Transactions> findAllByUserOrderByTransactionDateDesc(ApplicationUser user);

    boolean existsByCheckNumberAndUser(String checkNumber, ApplicationUser user);

    boolean existsByCheckNumberAndRevenueCentreId(String checkNumber, int revenueCentreId);

    List<Transactions> findAllByGroupIdAndTransactionTypeIdOrderByTransactionDateDesc(String groupId, String id);

    List<Transactions> findAllByGroupIdAndTransactionTypeInOrderByTransactionDateDesc(String id, List<TransactionType> transactionTypes);

    List<Transactions> findAllByTransactionTypeInOrderByTransactionDateDesc(List<TransactionType> transactionTypes);
    List<Transactions> findAllByTransactionTypeInAndGroupOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Group group);
    List<Transactions> findAllByTransactionTypeInOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Pageable pageable);
    List<Transactions> findAllByTransactionTypeInAndGroupOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Group group, Pageable pageable);

    List<Transactions> findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Date start, Date end);
    List<Transactions> findAllByTransactionTypeInAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Group group, Date start, Date end);
    List<Transactions> findAllByTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Date start, Date end, Pageable pageable);
    List<Transactions> findAllByTransactionTypeInAndGroupAndTransactionDateBetweenOrderByTransactionDateDesc(List<TransactionType> transactionTypes, Group group, Date start, Date end, Pageable pageable);

    List<Transactions> findAllByGroupIdAndTransactionTypeInAndTransactionDateBetweenOrderByTransactionDateDesc(String id, List<TransactionType> transactionTypes, Date start, Date end);

}

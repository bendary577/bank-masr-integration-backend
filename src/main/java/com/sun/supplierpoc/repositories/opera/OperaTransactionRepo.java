package com.sun.supplierpoc.repositories.opera;

import com.sun.supplierpoc.models.OperaTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OperaTransactionRepo extends MongoRepository<OperaTransaction, String> {
    List<OperaTransaction> findAllByAccountIdAndDeleted(String accountId, boolean deleted);
    List<OperaTransaction> findAllByAccountIdAndDeletedAndCreationDateBetween(String accountId, boolean deleted, Date from, Date to);

    int countByAccountIdAndDeletedAndStatus(String accountId, boolean deleted, String status);
    int countByAccountIdAndDeletedAndStatusAndCreationDateBetween(String accountId, boolean deleted, String status,
                                                                 Date from, Date to);

    List<OperaTransaction> findAllByAccountIdAndDeletedAndCardNumber(String id, boolean b, String cardNumber);

    List<OperaTransaction> findByAccountIdAndDeletedAndCreationDateBetween(String id, boolean b, Date start, Date end);

    List<OperaTransaction> findByAccountIdAndDeletedAndCardNumberAndCreationDateBetween(String id, boolean b, String cardNumber, Date start, Date end);
}

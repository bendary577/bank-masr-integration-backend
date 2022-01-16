package com.sun.supplierpoc.repositories.simphony;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.sun.supplierpoc.models.voucher.*;

import java.util.List;

@Repository
public interface VoucherTransRepo extends MongoRepository<VoucherTransaction, String> {

    boolean existsByCheckNumberAndRevenueCentreIdAndStatus(String checkNumber, int revenueCentreId, String paidTransaction);

    List<VoucherTransaction> findByVoucherIdAndAccountId(String voucherId, String id);

//    @Query(value = "{'VoucherTransaction.productType': {$regex: ?0, $options: 'i'}, 'sourceDescriptor': ?1}", count = true)
//    public Long countFetchedDocumentsForCategory(String cat, String sourceDescriptor);

//    @Query(value = "{'voucherTransaction.afterDiscount': {$regex: ?0, $options: 'i'}, 'sourceDescriptor': ?1}", count = true)
//    int getSucceedTransactionCount(String voucherId, String id);
//
//    @Query(value = "{'voucherTransaction.afterDiscount': {$regex: ?0, $options: 'i'}, 'sourceDescriptor': ?1}", count = true)
//    int getFailedTransactionCount(String voucherId, String id);
}

package com.sun.supplierpoc.repositories.simphony;

import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyCheck;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SplittableCheckRepo extends MongoRepository<SimphonyCheck, String> {

    Optional<SimphonyCheck> findByAccountIdAndRevenueCenterIdAndCheckNumber(String id, int revenueCentreId, String checkNumber);

    List<SimphonyCheck> findAllByAccountIdAndDeleted(String id, boolean deleted);

    List<SimphonyCheck> findByAccountIdAndDeletedAndCreationDateBetween(String id, boolean b, Date start, Date end);

    void findByAccountIdAndDeleted(String id, boolean deleted);
}

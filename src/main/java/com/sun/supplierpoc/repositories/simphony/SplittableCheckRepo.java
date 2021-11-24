package com.sun.supplierpoc.repositories.simphony;

import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyPaymentReq;
import com.sun.supplierpoc.models.simphony.simphonyCheck.SimphonyCheck;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SplittableCheckRepo extends MongoRepository<SimphonyCheck, String> {

    Optional<SimphonyCheck> findByAccountIdAndRevenueCenterIdAndCheckNumber(String id, int revenueCentreId, String checkNumber);
}

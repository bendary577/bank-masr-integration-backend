package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.SyncJobType;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncJobTypeRepo extends MongoRepository<SyncJobType, String>{

    SyncJobType findByNameAndAccountIdAndDeleted(String name, String accountId, boolean deleted);
    List<SyncJobType> findByAccountIdAndDeleted(String accountId, boolean deleted);

}

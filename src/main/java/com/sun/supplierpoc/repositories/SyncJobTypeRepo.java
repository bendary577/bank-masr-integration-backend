package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJobType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface SyncJobTypeRepo extends MongoRepository<SyncJobType, String>{

    SyncJobType findByNameAndAccountId(String name, String accountId);
    List<SyncJobType> findByAccountId(String accountId);

}

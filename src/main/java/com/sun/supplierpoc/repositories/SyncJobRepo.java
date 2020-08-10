package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.SyncJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncJobRepo extends MongoRepository<SyncJob, String>{
    List<SyncJob> findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(String syncJobTypeId, boolean deleted);
    List<SyncJob> findBySyncJobTypeIdAndStatusAndDeletedOrderByCreationDateDesc(String syncJobTypeId,String status,
                                                                                boolean deleted);

}

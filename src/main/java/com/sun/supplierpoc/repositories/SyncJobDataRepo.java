package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJobData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface SyncJobDataRepo extends MongoRepository<SyncJobData, String>{

    List<SyncJobData> findBySyncJobId(String syncJobId);
}

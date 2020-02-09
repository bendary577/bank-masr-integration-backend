package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJobData;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SyncJobDataRepo extends MongoRepository<SyncJobData, String>{
}

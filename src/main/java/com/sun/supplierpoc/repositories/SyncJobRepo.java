package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJob;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SyncJobRepo extends MongoRepository<SyncJob, String>{
}

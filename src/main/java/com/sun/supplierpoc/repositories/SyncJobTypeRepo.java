package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJobType;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SyncJobTypeRepo extends MongoRepository<SyncJobType, String>{
}

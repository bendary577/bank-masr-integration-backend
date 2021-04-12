package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Application;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApplicationRepo extends MongoRepository<Application, String> {

    List<Application> findAllByAccountIdAndDeleted(String accountId, boolean deleted);
}

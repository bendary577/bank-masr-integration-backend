package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface CompanyRepo extends MongoRepository<Company, String> {
}

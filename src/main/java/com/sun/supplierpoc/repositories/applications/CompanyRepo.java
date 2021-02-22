package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository

public interface CompanyRepo extends MongoRepository<Company, String> {
    ArrayList<Company> findAllByAccountID(String accountId);

    ArrayList<Company> findAllByAccountIDAndDeleted(String accountId, boolean deleted);
}

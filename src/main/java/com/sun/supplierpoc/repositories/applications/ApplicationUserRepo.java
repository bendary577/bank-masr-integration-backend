package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Company;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository

public interface ApplicationUserRepo extends MongoRepository<ApplicationUser, String> {
    ArrayList<ApplicationUser> findAll();
    ArrayList<ApplicationUser> findAllByCompany(Company company);

}

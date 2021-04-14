package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ApplicationUserRepo extends MongoRepository<ApplicationUser, String> {
    ArrayList<ApplicationUser> findAll();
    ApplicationUser findByIdAndDeleted(String id, boolean deleted);
    ApplicationUser findByCode(String code);

    ApplicationUser findFirstByEmailAndAccountId(String email, String accountId);
    ApplicationUser findFirstByEmailAndAccountIdAndDeleted(String email, String accountId, boolean deleted);

    List<ApplicationUser> findTop3ByAccountIdOrderByTopDesc(String accountId);

    ArrayList<ApplicationUser> findAllByAccountId(String accountId);
    List<ApplicationUser> findAllByAccountIdAndGroupAndDeleted(String accountId, Group group, boolean deleted);

}

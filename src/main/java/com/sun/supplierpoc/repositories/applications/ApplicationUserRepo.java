package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.ApplicationUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ApplicationUserRepo extends MongoRepository<ApplicationUser, String> {
    ArrayList<ApplicationUser> findAll();

    ArrayList<ApplicationUser> findAllByDeleted(boolean deleted);

    ArrayList<ApplicationUser> findAllByGroupId(String groupId);

    ApplicationUser findByCode(String code);

//    @Query(value = "SELECT * u from ApplicationUser ORDER BY u.top DESC LIMIT ?")
    List<ApplicationUser> findTop3ByAccountIdOrderByTopDesc(String accountId);

    ArrayList<ApplicationUser> findAllByAccountId(String accountId);
}

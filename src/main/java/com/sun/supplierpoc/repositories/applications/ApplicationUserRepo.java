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
    ApplicationUser findByCode(String code);
    ApplicationUser findFirstByEmailAndAccountId(String email, String accountId);

//    @Query(value = "SELECT * u from ApplicationUser ORDER BY u.top DESC LIMIT ?")
    List<ApplicationUser> findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(String accountId,boolean deleted, int top);

    ArrayList<ApplicationUser> findAllByAccountId(String accountId);

    List<ApplicationUser> findAllByAccountIdAndGroupAndDeleted(String accountId, Group group, boolean deleted);

}

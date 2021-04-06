package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepo extends MongoRepository<Group, String> {
    Group findFirstById(String id);
    ArrayList<Group> findAllByAccountId(String accountId);
    ArrayList<Group> findAllByAccountIdAndDeleted(String accountId, boolean deleted);
    List<Group> findTop3ByAccountIdOrderByTopDesc(String accountId);
    ArrayList<Group> findAllByAccountIdAndParentGroup(String id, Group group);

    boolean existsByName(String name);

    Optional<Group> findByName(String name);
}

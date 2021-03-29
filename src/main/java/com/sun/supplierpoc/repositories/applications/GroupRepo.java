package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface GroupRepo extends MongoRepository<Group, String> {
    Group findFirstById(String id);
    ArrayList<Group> findAllByAccountID(String accountId);
    ArrayList<Group> findAllByAccountIDAndDeleted(String accountId, boolean deleted);
}

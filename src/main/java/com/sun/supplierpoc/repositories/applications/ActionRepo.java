package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Action;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface ActionRepo extends MongoRepository<Action, String> {

    ArrayList<Action> findByAccountId(String accountId);
    ArrayList<Action> findByUserAndActionType(String userId, String actionType);
}

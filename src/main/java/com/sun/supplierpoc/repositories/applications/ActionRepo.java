package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;

@Repository
public interface ActionRepo extends MongoRepository<Action, String> {

    /* Get actions on account level */
    ArrayList<Action> findByAccountId(String accountId);
    ArrayList<Action> findByAccountIdAndDateBetween(String accountId, Date date, Date toDate);

    ArrayList<Action> findByAccountIdAndActionType(String accountId, String actionType);
    ArrayList<Action> findByAccountIdAndActionTypeAndDateBetween(String accountId, String actionType, Date date, Date toDate);

    /* Get actions on user level */
    ArrayList<Action> findByUser(User user);
    ArrayList<Action> findByUserAndDateBetween(User user, Date date, Date toDate);

    ArrayList<Action> findByUserAndActionType(User user, String actionType);
    ArrayList<Action> findByUserAndActionTypeAndDateBetween(User user, String actionType, Date date, Date toDate);
}

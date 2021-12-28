package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;

@Repository
public interface ActionRepo extends MongoRepository<Action, String> {

    /* Get actions on account level */
    int countByAccountId(String accountId);
    ArrayList<Action> findByAccountId(String accountId, Pageable pageable);

    int countByAccountIdAndDateBetween(String accountId, Date date, Date toDate);
    ArrayList<Action> findByAccountIdAndDateBetween(String accountId, Date date, Date toDate, Pageable pageable);

    int countByAccountIdAndActionType(String accountId, String actionType);
    ArrayList<Action> findByAccountIdAndActionType(String accountId, String actionType, Pageable pageable);

    int countByAccountIdAndActionTypeAndDateBetween(String accountId, String actionType, Date date, Date toDate);
    ArrayList<Action> findByAccountIdAndActionTypeAndDateBetween(String accountId, String actionType, Date date, Date toDate, Pageable pageable);

    /* Get actions on user level */
    int countByUser(User user);
    ArrayList<Action> findByUser(User user, Pageable pageable);

    int countByUserAndDateBetween(User user, Date date, Date toDate);
    ArrayList<Action> findByUserAndDateBetween(User user, Date date, Date toDate, Pageable pageable);

    int countByUserAndActionType(User user, String actionType);
    ArrayList<Action> findByUserAndActionType(User user, String actionType, Pageable pageable);

    int countByUserAndActionTypeAndDateBetween(User user, String actionType, Date date, Date toDate);
    ArrayList<Action> findByUserAndActionTypeAndDateBetween(User user, String actionType, Date date, Date toDate, Pageable pageable);
}

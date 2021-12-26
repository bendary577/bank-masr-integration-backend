package com.sun.supplierpoc.repositories.applications;

import com.sun.supplierpoc.models.applications.ActionStats;
import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface ActionStatsRepo extends MongoRepository<ActionStats, String> {
    ActionStats findByAgent(User agent);
    ArrayList<ActionStats> findByAccountId(String accountId);
}

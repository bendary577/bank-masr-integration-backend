package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.models.applications.ActionStats;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.applications.ActionStatsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ActionStatsService {
    @Autowired
    ActionStatsRepo actionStatsRepo;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ActionStats createActionStats(ActionStats stats){
        return actionStatsRepo.save(stats);
    }

    public ActionStats findActionStatsByAgent(User agent){
        return actionStatsRepo.findByAgent(agent);
    }

    public ArrayList<ActionStats> findActionStatsByAccount(String accountId){
        return actionStatsRepo.findByAccountId(accountId);
    }

}

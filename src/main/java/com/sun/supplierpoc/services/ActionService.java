package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.repositories.applications.ActionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ActionService {
    @Autowired
    ActionRepo actionRepo;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Action createUserAction(Action action){
        action = actionRepo.save(action);
        return action;
    }

    public ArrayList<Action> getUserAction(String userId, String accountId, String actionType){
        ArrayList<Action> actions = new ArrayList<>();
        try{
            if(userId.equals("") && actionType.equals("")){
                actions = actionRepo.findByAccountId(accountId);
            }else{
                actions = actionRepo.findByUserAndActionType(userId, actionType);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actions;
    }
}

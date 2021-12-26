package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.applications.ActionStats;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.applications.ActionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@Service
public class ActionService {
    @Autowired
    ActionRepo actionRepo;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Action createUserAction(Action action){
        action = actionRepo.save(action);
        return action;
    }

    public ArrayList<Action> getUserAction(User user, String accountId, String actionType,
                                           String from, String to){
        ArrayList<Action> actions = new ArrayList<>();
        Date fromDate = null;
        Date toDate = null;
        try{
            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && !to.equals("")){
                DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            }
            /* On account level */
            if(user == null){
                if(actionType.equals("")){
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.findByAccountId(accountId);
                    else
                        actions = actionRepo.findByAccountIdAndDateBetween(accountId, fromDate, toDate);
                }else {
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.findByAccountIdAndActionType(accountId, actionType);
                    else
                        actions = actionRepo.findByAccountIdAndActionTypeAndDateBetween(accountId, actionType, fromDate, toDate);
                }
            }else{
                if(actionType.equals(""))
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.findByUser(user);
                    else
                        actions = actionRepo.findByUserAndDateBetween(user, fromDate, toDate);
                else {
                    actions = actionRepo.findByUserAndActionTypeAndDateBetween(user, actionType, fromDate, toDate);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actions;
    }
}

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.applications.Action;
import com.sun.supplierpoc.models.applications.ActionStats;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.applications.ActionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public int getUserActionCount(User user, String accountId, String actionType,
                                           String from, String to){
        int actions = 0;
        Date fromDate = null;
        Date toDate = null;
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");

        try{
            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && to.equals("")){
                to = dateFormat.format(new Date());
            }

            if(!from.equals("") && !to.equals("")){
                // Add one day to range to include last day selected
                Calendar c = Calendar.getInstance();
                c.setTime(dateFormat.parse(to));
                c.add(Calendar.DATE, 1);
                to = dateFormat.format(c.getTime());

                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            }
            /* On account level */
            if(user == null){
                if(actionType.equals("")){
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.countByAccountId(accountId);
                    else
                        actions = actionRepo.countByAccountIdAndDateBetween(accountId, fromDate, toDate);
                }else {
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.countByAccountIdAndActionType(accountId, actionType);
                    else
                        actions = actionRepo.countByAccountIdAndActionTypeAndDateBetween(accountId, actionType, fromDate, toDate);
                }
            }else{
                if(actionType.equals(""))
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.countByUser(user);
                    else
                        actions = actionRepo.countByUserAndDateBetween(user, fromDate, toDate);
                else {
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.countByUserAndActionType(user, actionType);
                    else
                        actions = actionRepo.countByUserAndActionTypeAndDateBetween(user, actionType, fromDate, toDate);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actions;
    }

    public ArrayList<Action> getUserActionPaginated(User user, String accountId, String actionType,
                                                    String from, String to, int pageNumber, int limit){
        ArrayList<Action> actions = new ArrayList<>();
        Date fromDate = null;
        Date toDate = null;
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");

        try{
            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && to.equals("")){
                to = dateFormat.format(new Date());
            }

            if(!from.equals("") && !to.equals("")){
                // Add one day to range to include last day selected
                Calendar c = Calendar.getInstance();
                c.setTime(dateFormat.parse(to));
                c.add(Calendar.DATE, 1);
                to = dateFormat.format(c.getTime());

                fromDate = dateFormat.parse(from);
                toDate = dateFormat.parse(to);
            }
            Pageable paging = PageRequest.of(pageNumber-1, limit);
            /* On account level */
            if(user == null){
                if(actionType.equals("")){
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.findByAccountId(accountId, paging);
                    else
                        actions = actionRepo.findByAccountIdAndDateBetween(accountId, fromDate, toDate, paging);
                }else {
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.findByAccountIdAndActionType(accountId, actionType, paging);
                    else
                        actions = actionRepo.findByAccountIdAndActionTypeAndDateBetween(accountId, actionType, fromDate, toDate, paging);
                }
            }else{
                if(actionType.equals(""))
                    if(fromDate == null && toDate == null)
                        actions = actionRepo.findByUser(user, paging);
                    else
                        actions = actionRepo.findByUserAndDateBetween(user, fromDate, toDate, paging);
                else {
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.findByUserAndActionType(user, actionType, paging);
                    else
                        actions = actionRepo.findByUserAndActionTypeAndDateBetween(user, actionType, fromDate, toDate, paging);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actions;
    }

    public ArrayList<Action> getUserAction(User user, String accountId, String actionType,
                                                    String from, String to){
        DateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd");
        ArrayList<Action> actions = new ArrayList<>();
        Date fromDate = null;
        Date toDate = null;
        try{
            // 2021-12-01 yyyy-mm-dd
            if(!from.equals("") && to.equals("")){
                to = dateFormat.format(new Date());
            }
            if(!from.equals("") && !to.equals("")){
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
                    if (fromDate == null && toDate == null)
                        actions = actionRepo.findByUserAndActionType(user, actionType);
                    else
                        actions = actionRepo.findByUserAndActionTypeAndDateBetween(user, actionType, fromDate, toDate);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return actions;
    }
}

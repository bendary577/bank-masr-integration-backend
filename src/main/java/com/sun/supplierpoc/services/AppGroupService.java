package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppGroupService {

    @Autowired
    GroupRepo groupRepo;


    public List<Group> getTopGroups(Account account) {

        List<Group> groups = groupRepo.findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(account.getId(), false, 0);

        return groups;
    }
}

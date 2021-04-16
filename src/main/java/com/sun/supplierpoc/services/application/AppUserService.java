package com.sun.supplierpoc.services.application;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.repositories.applications.ApplicationUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {

    @Autowired
    private ApplicationUserRepo userRepo;


    public List<ApplicationUser> getTopUsers(Account account) {

            List<ApplicationUser> applicationUsers = userRepo.findTop3ByAccountIdAndDeletedAndTopNotOrderByTopDesc(account.getId(),false,  0);

            return applicationUsers;
    }
    
}

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.applications.ApplicationUser;
import com.sun.supplierpoc.models.applications.Balance;
import com.sun.supplierpoc.models.applications.Group;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.models.configurations.RevenueCenter;
import com.sun.supplierpoc.repositories.UserRepo;
import com.sun.supplierpoc.repositories.applications.GroupRepo;
import com.sun.supplierpoc.services.application.AppUserService;
import com.sun.supplierpoc.services.application.WalletService;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppGroupService {

    @Autowired
    GroupRepo groupRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private WalletService walletService;

    public List<Group> getTopGroups(Account account) {

        List<Group> groups = groupRepo.findAllByAccountIdAndDeletedAndTopNotOrderByTopDesc(account.getId(), false, 0);

        return groups;
    }
    public Group saveGroup(Group group) {
        Group groups = groupRepo.save(group);
        return groups;
    }


    public void resetGroupWallet(Account account, Group group, ArrayList<RevenueCenter> revenueCenters) {
        User user = userRepo.findByAccountId(account.getId()).get(0);
        List<ApplicationUser> appUsers = appUserService.getActiveUsersByGroup(account.getId(), group);

       for(ApplicationUser applicationUser : appUsers){

           if(applicationUser.getGroup().getId().equals(group.getId())){
               Balance canteenChargeBalance = new Balance();
               canteenChargeBalance.setAmount(Double.parseDouble(group.getCanteenConfiguration().getChargeAmount()));
               canteenChargeBalance.setRevenueCenters(revenueCenters);

               walletService.resetWallet(user, applicationUser, canteenChargeBalance, group.getCanteenConfiguration().isAccumulate());
           }
       }

    }
}

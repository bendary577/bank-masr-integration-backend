package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.FeatureRepository;
import com.sun.supplierpoc.repositories.RoleRepository;
import com.sun.supplierpoc.repositories.UserRepo;
import org.apache.tomcat.util.bcel.Const;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    UserRepo userRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private Conversions conversions;

    public Response addRole(Role roleRequest) {

        Response response = new Response();

        try {
            Optional<Feature> featureOptional = featureRepository.findById(roleRequest.getFeatureId());
            if(featureOptional.isPresent()) {
                Feature feature = featureOptional.get();
                Role role = roleRepository.save(roleRequest);
                feature.getRoles().add(role);
                featureRepository.save(feature);
                response.setStatus(true);
                response.setData(role);
                return response;
            }else{
                response.setStatus(false);
                response.setMessage(Constants.INVALID_FEATURE_ID + roleRequest.getFeatureId());
                return response;
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(RoleService.class).info(e.getMessage());
            response.setStatus(false);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public List<Role> getAllRoles(User user) {

        List<Role> roleList = roleRepository.findByIdIn(user.getRoles());

        return roleList;
    }

    public Response addUserRole(String userId, List<String> roleIds) {

        Response response = new Response();
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isPresent()) {
            try {

                User user = userOptional.get();
                List<Role> roles = roleRepository.findAllByIdIn(roleIds);

                // Check if account have the features of these roles
                Optional<Account> accountOptional = accountService.getAccountOptional(user.getAccountId());
                if(accountOptional.isPresent()) {
                    Account account = accountOptional.get();
                    for(Role role : roles){
                        if(!conversions.checkIfAccountHasFeature(account.getFeatures(), role.getFeature())){
                            response.setMessage("Account " + account.getName() + " doesn't have the feature of "
                                    + role.getFeature().getName());
                            response.setStatus(false);
                            return response;
                        }
                    }

                    //Save roles into user
                    user.getRoles().addAll(roles);
                    userRepo.save(user);

                    response.setStatus(true);
                    response.setData(user);
                }else{
                    response.setMessage(Constants.NOT_ELIGIBLE_ACCOUNT);
                    response.setStatus(false);
                    }
            } catch (Exception e) {
                response.setMessage(e.getMessage());
                response.setStatus(false);
            }
        } else {
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
        }
        return response;
    }

    public Response getUserRoles(String userId, boolean sameUser, User authedUser) {

        Response response = new Response();
        User user;
        List<Role> roleList;
        Optional<User> userOptional;
        if (sameUser) {
            userOptional = userRepo.findById(authedUser.getId());
        } else {
            userOptional = userRepo.findById(userId);
        }
        if (userOptional.isPresent()) {
            user = userOptional.get();
            roleList = user.getRoles();
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return response;
        }
        response.setStatus(true);
        response.setData(user);
        return response;
    }

    public boolean hasRole(User authedUser, String chargeWallet) {
        User user = userRepo.findById(authedUser.getId())
                .orElseThrow(() -> new RuntimeException("An Unexpected Error Accord."));
        List<Role> roles = user.getRoles();
        boolean hasRole = conversions.checkIfUserHasRole(roles, chargeWallet);
        return hasRole;
    }
}

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
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
    private Conversions conversions;

    public Role addRole(Role roleRequest) {

        try {
            Role role = roleRepository.save(roleRequest);
            return role;
        } catch (Exception e) {
            LoggerFactory.getLogger(RoleService.class).info(e.getMessage());
            return new Role();
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
                user.getRoles().addAll(roles);
                userRepo.save(user);
                response.setStatus(true);
                response.setData(user);
                return response;
            } catch (Exception e) {
                response.setMessage(e.getMessage());
                response.setStatus(false);
                return response;
            }
        } else {
            response.setMessage(Constants.INVALID_USER);
            response.setStatus(false);
            return response;
        }

    }

    public Response getUserRoles(String userId, boolean sameUser, User authedUser) {

        Response response = new Response();
        List<Role> roleList;
        Optional<User> userOptional;

        if (sameUser) {
            userOptional = userRepo.findById(authedUser.getId());
        } else {
            userOptional = userRepo.findById(userId);
        }

        if (userOptional.isPresent()) {
            roleList = userOptional.get().getRoles();
        } else {
            response.setStatus(false);
            response.setMessage(Constants.INVALID_USER);
            return response;
        }

        response.setStatus(true);
        response.setData(roleList);
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

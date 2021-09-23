package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.RoleRepository;
import com.sun.supplierpoc.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepository roleRepository;

    public boolean eligibleForRole(User user, String roleRequest) {

        Role role = roleRepository.findByName(roleRequest);
        //user.getRoleIds().contains(role.getId())
        if(true){
            return true;
        }else{
            return false;
        }

    }

}

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.Role;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.RoleRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role addRole(Role roleRequest) {

        try{
            Role role = roleRepository.save(roleRequest);
            return role;
        }catch (Exception e){
            LoggerFactory.getLogger(RoleService.class).info(e.getMessage());
            return new Role();
        }
    }

    public List<Role> getAllRoles(User user) {

        List<Role> roleList = roleRepository.findAllByIdIn(user.getRoleIds());

        return roleList;
    }
}

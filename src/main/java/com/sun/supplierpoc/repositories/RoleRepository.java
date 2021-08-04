package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByName(String name);

    List<Role> findAllByIdIn(List<String> roleIds);
}

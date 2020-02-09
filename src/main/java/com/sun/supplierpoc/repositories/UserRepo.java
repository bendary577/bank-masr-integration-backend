package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.User;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepo extends MongoRepository<User, String>{

    User findByName(String Name);
    List<User> findAllByName(String Name);

}

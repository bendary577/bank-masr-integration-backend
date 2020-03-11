package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface UserRepo extends MongoRepository<User, String>{

    List<User> findByAccountId(String accountId);
}

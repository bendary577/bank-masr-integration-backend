package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.UserOld;
import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepo extends MongoRepository<User, String>{

    List<User> findByAccountId(String accountId);
}

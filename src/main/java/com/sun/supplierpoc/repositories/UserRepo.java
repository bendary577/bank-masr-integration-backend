package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.auth.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserRepo extends MongoRepository<User, String>{

    List<User> findByAccountIdAndDeleted(String accountId, boolean deleted);

    User findByUsername(String admin);

    User findByEmail(String email);


    Optional<User> findByNameAndAccountId(String name, String id);

    boolean existsByNameAndAccountId(String name, String id);

    boolean existsByUsernameAndAccountId(String username, String id);

    List<User> findByAccountId(String id);
}

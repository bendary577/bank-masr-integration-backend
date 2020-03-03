package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository

public interface AccountRepo extends MongoRepository<Account, String>{
    Optional<Account> findById(String accountId);
    List<Account> findAll();
}

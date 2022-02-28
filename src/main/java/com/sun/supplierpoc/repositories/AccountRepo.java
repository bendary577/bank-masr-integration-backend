package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.Feature;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository

public interface AccountRepo extends MongoRepository<Account, String>{
    Optional<Account> findByIdAndDeleted(String accountId, boolean deleted);
    boolean existsAccountByNameAndDeleted(String accountName, boolean deleted);
    List<Account> findAll();
    ArrayList<Account> findByDeleted(boolean deleted);
    ArrayList<Account> findByDeletedAndFeatures(boolean deleted, Feature feature);
}

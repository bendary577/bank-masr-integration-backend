package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.Account;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface AccountRepo extends MongoRepository<Account, String>{
}

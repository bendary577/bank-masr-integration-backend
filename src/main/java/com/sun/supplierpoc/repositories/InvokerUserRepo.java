package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.auth.InvokerUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InvokerUserRepo extends MongoRepository<InvokerUser, String> {
    InvokerUser findByUsernameAndPassword(String username, String password);
}

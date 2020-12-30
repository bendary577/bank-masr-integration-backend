package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.auth.InvokerUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface InvokerUserRepo extends MongoRepository<InvokerUser, String> {
    InvokerUser findByUsernameAndPassword(String username, String password);
    ArrayList<InvokerUser> findAllBySyncJobTypeId(String syncJobTypeId);
    int countAllByUsername(String username);
}

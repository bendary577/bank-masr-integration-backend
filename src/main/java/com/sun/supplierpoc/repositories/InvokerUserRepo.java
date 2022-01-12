package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.auth.InvokerUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface InvokerUserRepo extends MongoRepository<InvokerUser, String> {
    InvokerUser findByUsernameAndPassword(String username, String password);
    ArrayList<InvokerUser> findAllByTypeId(String syncJobTypeId);
    ArrayList<InvokerUser> findAllByAccountId(String accountId);

    int countAllByUsername(String username);
    int countAllByUsernameAndAccountId(String username, String accountId);

}

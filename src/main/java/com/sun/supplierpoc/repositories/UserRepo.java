package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.UserOld;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface UserRepo extends MongoRepository<UserOld, String>{

    UserOld findByName(String Name);
    List<UserOld> findAllByName(String Name);

}

package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJobData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface AccountRepo extends MongoRepository<Account, String>{

}

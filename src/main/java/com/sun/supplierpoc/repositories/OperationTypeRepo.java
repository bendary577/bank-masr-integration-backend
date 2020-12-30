package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.OperationTypes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface OperationTypeRepo extends MongoRepository<OperationTypes, String> {
    ArrayList<OperationTypes> findAllByAccountIdAndDeletedOrderByIndexAsc(String accountID, boolean deleted);
    OperationTypes findAllByNameAndAccountIdAndDeleted(String name, String accountID, boolean deleted);
}

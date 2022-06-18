package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.OperationType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.ArrayList;

public interface OperationTypeRepo extends MongoRepository<OperationType, String> {
    ArrayList<OperationType> findAllByAccountIdAndDeletedOrderByIndexAsc(String accountID, boolean deleted);
    ArrayList<OperationType> findAllByAccountIdAndDeleted(String accountID, boolean deleted);
    OperationType findAllByNameAndAccountIdAndDeleted(String name, String accountID, boolean deleted);
    OperationType findAllByName(String name);
}
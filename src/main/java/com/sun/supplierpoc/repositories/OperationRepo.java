package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.Operation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OperationRepo extends MongoRepository<Operation, String> {
    List<Operation> findByoperationTypeIdAndDeletedOrderByCreationDateDesc(String id, boolean b);
}

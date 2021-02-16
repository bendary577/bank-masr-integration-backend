package com.sun.supplierpoc.repositories;

import com.sun.supplierpoc.models.OperationData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OperationDataRepo extends MongoRepository<OperationData, String> {
    List<OperationData> findByOperationIdAndDeleted(String operationId, boolean b);

    OperationData findOperationDataByOperationIdAndDeleted(String operationId, boolean b);
}

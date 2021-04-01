package com.sun.supplierpoc.repositories;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface SyncJobDataRepo extends MongoRepository<SyncJobData, String>{

    @Query("{'data.bookingNo' : ?0}")
    List<SyncJobData> findByDataByBookingNo(String bookingNumber);

    @Query("{'data.bookingNo' : ?0}")
    List<SyncJobData> findByDataByBookingNoAndSyncJobId(String bookingNumber, String syncJobId);

    List<SyncJobData> findBySyncJobIdAndDeleted(String syncJobId, boolean deleted);
    List<SyncJobData> findBySyncJobIdAndDeletedAndStatus(String syncJobId, boolean deleted, String status);
    List<SyncJobData> deleteAllBySyncJobId(String syncJobId);
}

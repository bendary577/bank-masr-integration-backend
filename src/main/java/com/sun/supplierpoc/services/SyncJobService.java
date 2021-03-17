package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class SyncJobService {
    @Autowired
    private SyncJobRepo syncJobRepo;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void saveSyncJobStatus(SyncJob syncJob, int rowsCount, String message, String status){
        syncJob.setStatus(status);
        syncJob.setReason(message);
        syncJob.setEndDate(new Date());
        syncJob.setRowsFetched(rowsCount);
        syncJobRepo.save(syncJob);
    }

    public SyncJob getSyncJobByRevenueCenterID(int revenueCenterID, String syncJobTypeID){
        try {
            List<SyncJob> syncJobs = syncJobRepo.findSyncJobByStatusAndRevenueCenterAndSyncJobTypeIdAndDeletedOrderByCreationDateDesc(Constants.SUCCESS,
                    revenueCenterID, syncJobTypeID, false);
            return  syncJobs.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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
}

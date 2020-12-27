package com.sun.supplierpoc.services;

import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncJobDataService {

    @Autowired
    SyncJobDataRepo syncJobDataRepo;

    public void updateSyncJobDataStatus(List<SyncJobData> syncJobDataArrayList, String status) {
        for (SyncJobData syncJobData : syncJobDataArrayList) {
            syncJobData.setStatus(status);
            syncJobDataRepo.save(syncJobData);
        }
    }

    public ArrayList<SyncJobData> getSyncJobData(String syncJobID){
        return (ArrayList<SyncJobData>) syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobID, false);
    }

}

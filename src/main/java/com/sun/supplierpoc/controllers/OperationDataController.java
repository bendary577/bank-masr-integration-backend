package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationData;
import com.sun.supplierpoc.repositories.OperationDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OperationDataController {


    @Autowired
    private OperationDataRepo operationDataRepo;

    @GetMapping("/getOperationDataById")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<OperationData> getOpertionData(@RequestParam(name = "operationId") String operationId){

        List<OperationData> operationData = operationDataRepo.findByOperationIdAndDeleted(operationId, false);

        return operationData;
    }


//
//    public List<OperationData> get(@RequestParam(name = "syncJobId") String syncJobId)  {
//        List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId, false);
//        return syncJobData;
//    }
//
//
//
//
//    public ArrayList<SyncJobData> getSyncJobData(String syncJobTypeId)  {
//        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
//        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
//        for (SyncJob syncJob : syncJobs) {
//            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
//            syncJobsData.addAll(syncJobData);
//        }
//        return syncJobsData;
//    }
//
//    public ArrayList<SyncJobData> getFailedSyncJobData(String syncJobTypeId)  {
//        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
//        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
//        for (SyncJob syncJob : syncJobs) {
//            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeletedAndStatus(syncJob.getId(),
//                    false, Constants.FAILED);
//            syncJobsData.addAll(syncJobData);
//        }
//        return syncJobsData;
//    }

}

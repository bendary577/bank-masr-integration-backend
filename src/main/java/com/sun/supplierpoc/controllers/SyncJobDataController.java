package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
// @RequestMapping(path = "server")


public class SyncJobDataController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    @GetMapping("/getSyncJobDataById")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJobData> getSyncJobDataById(@RequestParam(name = "syncJobId") String syncJobId)  {
        List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJobId, false);
        return syncJobData;
    }

    public ArrayList<SyncJobData> getSyncJobData(String syncJobTypeId)  {
        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
            syncJobsData.addAll(syncJobData);
        }
        return syncJobsData;
    }

}

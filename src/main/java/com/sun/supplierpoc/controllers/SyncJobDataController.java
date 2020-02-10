package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class SyncJobDataController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    @GetMapping("/getSyncJobDataById")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJobData> getSyncJobDataById(@RequestParam(name = "syncJobId") String syncJobId)  {
        List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobId(syncJobId);
        return syncJobData;
    }

}

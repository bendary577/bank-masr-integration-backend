package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class SyncJobController {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    @GetMapping("/getSyncJobs")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJob> getSyncJobs(@RequestParam(name = "typeName") String syncJobTypeId)  {
        SyncJobType syncJobType =  syncJobTypeRepo.findByNameAndAccountId(syncJobTypeId, "1");
        List<SyncJob> SyncJobs = syncJobRepo.findBySyncJobTypeIdOrderByCreationDateDesc(syncJobType.getId());
        return SyncJobs;
    }

}

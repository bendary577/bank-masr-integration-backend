package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class SyncJobTypeController {

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @GetMapping("/getAccSyncJobTypesByName")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public SyncJobType getSyncJobType(@RequestParam(name = "typeName") String syncJobTypeName)  {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountId(syncJobTypeName, "1");
        return syncJobType;
    }

    @GetMapping("/getSyncJobTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJobType> getSyncJobTypes()  {
        return syncJobTypeRepo.findByAccountId("1");
    }

    @PutMapping("/updateSyncJobTypesConfiguration")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public Boolean updateSyncJobTypesConfiguration(@RequestBody SyncJobType syncJobType)  {
        syncJobTypeRepo.save(syncJobType);
        return true;
    }

}

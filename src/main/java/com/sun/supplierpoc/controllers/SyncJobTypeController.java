package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController

public class SyncJobTypeController {

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;

    @GetMapping("/getSyncJobTypes")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJobType> getSyncJobTypes()  {
        List<SyncJobType> syncJobType = syncJobTypeRepo.findByAccountId("1");
        return syncJobType;
    }

}

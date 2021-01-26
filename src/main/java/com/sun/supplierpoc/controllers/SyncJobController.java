package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.models.OperationType;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.OperationTypeRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class SyncJobController {

    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;


    @GetMapping("/getSyncJobs")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJob> getSyncJobs(@RequestParam(name = "typeName") String syncJobTypeId, Principal principal)  {
        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        SyncJobType syncJobType =  syncJobTypeRepo.findByNameAndAccountIdAndDeleted(syncJobTypeId, user.getAccountId(), false);
        return syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobType.getId(), false);
    }

}

package com.sun.supplierpoc.controllers;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.Account;
import com.sun.supplierpoc.models.SyncJob;
import com.sun.supplierpoc.models.SyncJobData;
import com.sun.supplierpoc.models.SyncJobType;
import com.sun.supplierpoc.models.auth.User;
import com.sun.supplierpoc.repositories.AccountRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
// @RequestMapping(path = "server")


public class SyncJobDataController {
    @Autowired
    private SyncJobRepo syncJobRepo;
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private AccountRepo accountRepo;

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

    public ArrayList<SyncJobData> getFailedSyncJobData(String syncJobTypeId)  {
        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobTypeId, false);
        ArrayList<SyncJobData> syncJobsData = new ArrayList<>();
        for (SyncJob syncJob : syncJobs) {
            List<SyncJobData> syncJobData = syncJobDataRepo.findBySyncJobIdAndDeletedAndStatus(syncJob.getId(),
                    false, Constants.FAILED);
            syncJobsData.addAll(syncJobData);
        }
        return syncJobsData;
    }


    /*
    *
    * Delete all sync jobs and its data, except suppliers
    *
    * */
    @GetMapping("/clearSyncJobData")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public ResponseEntity<Boolean> clearSyncJobData(Principal principal){
        try {
            User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
            Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
            Account account = accountOptional.get();

            List<SyncJobType> syncJobTypes = syncJobTypeRepo.findByAccountIdAndDeleted(account.getId(), false);
            for (SyncJobType syncJobType : syncJobTypes) {
                if(syncJobType.getName().equals(Constants.SUPPLIERS))
                    continue;

                List<SyncJob>  syncJobs =
                        syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobType.getId(), false);
                for (SyncJob syncJob : syncJobs) {
                    syncJobDataRepo.deleteAllBySyncJobId(syncJob.getId());
                }
                syncJobRepo.deleteAllBySyncJobTypeId(syncJobType.getId());
            }

            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

}

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

    @GetMapping("/getSyncJobDataByBookingNo")
    @CrossOrigin(origins = "*")
    @ResponseBody
    public List<SyncJobData> getSyncJobDataByBookingNo(@RequestParam(name = "bookingNo", required=false) String bookingNo,
                                                       @RequestParam(name = "bookingStatus", required=false) String bookingStatus,
                                                       Principal principal)  {
        SyncJobType bookingSyncType;
        List<SyncJobData> syncJobData = new ArrayList<>();
        List<SyncJobData> data;

        User user = (User)((OAuth2Authentication) principal).getUserAuthentication().getPrincipal();
        Optional<Account> accountOptional = accountRepo.findById(user.getAccountId());
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();

            // Get all booking entries
            if(bookingStatus.equals("cancel"))
                bookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);
            else
                bookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);

            List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(bookingSyncType.getId(), false);
            for (SyncJob syncJob : syncJobs) {
                if(bookingNo == null || bookingNo.equals("")){
                    data = syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false);
                }else {
                    data = syncJobDataRepo.findByBookingNoAndSyncJobId(bookingNo, syncJob.getId());
                }
                syncJobData.addAll(data);
            }
        }
        return syncJobData;
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

package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.opera.Reservation;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class ReservationService {

    @Autowired
    private SyncJobDataRepo syncJobDataRepo;

    @Autowired
    private SyncJobRepo syncJobRepo;

    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;


    public Response syncReservation(String userId, Account account, MultipartFile file) {

        Response response = new Response();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.wLsIntegration, account.getId(), false);

        SyncJob syncJob = new SyncJob();

        try {

            syncJob = new SyncJob("success", "", new Date(), null, userId, account.getId(),
                    syncJobType.getId(), 0);

            syncJobRepo.save(syncJob);

            List<SyncJobData> syncJobData = ExcelHelper.excelToTutorials(syncJob, file.getInputStream());

            syncJob.setEndDate(new Date());
            syncJob.setRowsFetched(syncJobData.size());
            syncJobDataRepo.saveAll(syncJobData);

        } catch (IOException e) {
            syncJob.setStatus("failed");
            syncJob.setEndDate(new Date());
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }

        syncJobRepo.save(syncJob);
        return response;
    }
}

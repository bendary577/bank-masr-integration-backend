package com.sun.supplierpoc.services;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
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


    public Response syncReservation(String userId, Account account) throws IOException {

        Response response = new Response();

        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.wLsIntegration, account.getId(), false);

        SyncJob syncJob = new SyncJob("Success", "", new Date(System.currentTimeMillis()), null, userId, account.getId(),
                syncJobType.getId(), 0);

        syncJobRepo.save(syncJob);

            try {

                File file = new File("src/main/resources/reserv.xlsx");
                FileInputStream input = new FileInputStream(file);
                MultipartFile multipartFile = new MockMultipartFile("file",
                        file.getName(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", IOUtils.toByteArray(input));

                List<SyncJobData> syncJobData = ExcelHelper.excelToTutorials(syncJob, multipartFile.getInputStream());

                syncJob.setEndDate(new Date(System.currentTimeMillis()));
                syncJob.setRowsFetched(syncJobData.size());
                syncJobDataRepo.saveAll(syncJobData);
                response.setStatus(true);
                response.setMessage("Sync the file successfully");

            } catch (IOException e) {
                syncJob.setStatus("Failed");
                syncJob.setEndDate(new Date(System.currentTimeMillis()));
                response.setStatus(true);
                response.setMessage("fail to store excel data: " + e.getMessage());
            }

            syncJobRepo.save(syncJob);
            return response;
        }
}

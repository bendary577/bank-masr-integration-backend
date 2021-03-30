package com.sun.supplierpoc.services.opera;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
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
import java.util.Date;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    SyncJobDataRepo syncJobDataRepo;

    @Autowired
    private GeneralSettingsRepo generalSettingsRepo;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response fetchNewBookingFromReport(String userId, Account account){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        try{
            GeneralSettings generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), syncJobType.getId(), 0);
            syncJobRepo.save(syncJob);
        } catch (Exception e) {
            message = "Failed to establish a connection with the database.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        try{
            String fileName = "New Booking Details.xlsx";
            String filePath = "Saudi/";
            File file = new File(filePath + fileName);

            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", IOUtils.toByteArray(input));

            ExcelHelper excelHelper = new ExcelHelper();

            List<SyncJobData> syncJobData = excelHelper.getNewBookingFromExcel(syncJob, multipartFile.getInputStream());

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

            message = "Sync new booking successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync new booking.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    public Response fetchCancelBookingFromReport(String userId, Account account, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        GeneralSettings generalSettings;
        try{
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), syncJobType.getId(), 0);
            syncJobRepo.save(syncJob);
        } catch (Exception e) {
            message = "Failed to establish a connection with the database.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        try{
            String filePath = bookingConfiguration.filePath;
            String municipalityTax = bookingConfiguration.municipalityTax;
            File file = new File(filePath);

            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file", file.getName(),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", IOUtils.toByteArray(input));

            ExcelHelper excelHelper = new ExcelHelper();

            List<SyncJobData> syncJobData = excelHelper.getCancelBookingFromExcel(syncJob, municipalityTax,
                    generalSettings.getPaymentTypes(), multipartFile.getInputStream());

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

            message = "Sync cancel booking successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync cancel booking.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }
}

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

    @Autowired
    ExcelHelper excelHelper;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response fetchNewBookingFromReport(String userId, Account account){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;

        try{
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
            bookingConfiguration = syncJobType.getConfiguration().bookingConfiguration;

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

            List<SyncJobData> syncJobData = excelHelper.getNewBookingFromExcel(syncJob, municipalityTax,
                    generalSettings, input);

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

    public Response fetchCancelBookingFromReport(String userId, Account account){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;
        try{
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);
            bookingConfiguration = syncJobType.getConfiguration().bookingConfiguration;


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

            List<SyncJobData> syncJobData = excelHelper.getCancelBookingFromExcel(syncJob, municipalityTax,
                    generalSettings.getPaymentTypes(), generalSettings.getCancelReasons(), input);

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

    public Response fetchOccupancyFromReport(String userId, Account account){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        BookingConfiguration bookingConfiguration;
        try{
            SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.OCCUPANCY_UPDATE_REPORT, account.getId(), false);
            bookingConfiguration = syncJobType.getConfiguration().bookingConfiguration;

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
            File file = new File(filePath);

            FileInputStream input = new FileInputStream(file);
            List<SyncJobData> syncJobData = excelHelper.getOccupancyFromExcel(syncJob, input);

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

            message = "Sync occupancy Updates successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync occupancy Updates.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    public Response fetchExpensesDetailsFromReport(String userId, Account account){
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        BookingConfiguration bookingConfiguration;
        SyncJobType expensesDetailsSyncType;
        GeneralSettings generalSettings;

        try{
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            expensesDetailsSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.EXPENSES_DETAILS_REPORT, account.getId(), false);
            bookingConfiguration = expensesDetailsSyncType.getConfiguration().bookingConfiguration;

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(System.currentTimeMillis()), null,
                    userId, account.getId(), expensesDetailsSyncType.getId(), 0);
            syncJobRepo.save(syncJob);

        } catch (Exception e) {
            message = "Failed to establish a connection with the database.";
            response.setMessage(message);
            response.setStatus(false);
            return response;
        }

        try{
            String filePath = bookingConfiguration.filePath;
            File file = new File(filePath);

            FileInputStream input = new FileInputStream(file);
            List<SyncJobData> syncJobData = excelHelper.getExpensesUpdateFromExcel(syncJob, input,
                    generalSettings);

            syncJob.setStatus(Constants.SUCCESS);
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJob.setRowsFetched(syncJobData.size());
            syncJobRepo.save(syncJob);

            syncJobDataRepo.saveAll(syncJobData);

            message = "Sync expenses details successfully.";
            response.setStatus(true);
            response.setMessage(message);
        } catch (Exception e) {
            e.printStackTrace();

            syncJob.setStatus(Constants.FAILED);
            syncJob.setReason(e.getMessage());
            syncJob.setEndDate(new Date(System.currentTimeMillis()));
            syncJobRepo.save(syncJob);

            message = "Failed to sync expenses details.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }
}

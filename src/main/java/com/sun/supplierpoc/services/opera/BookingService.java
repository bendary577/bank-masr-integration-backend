package com.sun.supplierpoc.services.opera;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.*;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.GeneralSettingsRepo;
import com.sun.supplierpoc.repositories.SyncJobDataRepo;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Autowired
    ExpensesXMLHelper expensesXMLHelper;

    @Autowired
    NewBookingExcelHelper bookingExcelHelper;

    @Autowired
    CancelBookingExcelHelper cancelBookingExcelHelper;

    @Autowired
    OccupancyXMLHelper occupancyXMLHelper;

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Response fetchNewBookingFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        SyncJobType syncJobType;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;

        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);

            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
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

        try {
            DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = fileDateFormat.format(new Date());

            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
            String filePath = Constants.REPORTS_BUCKET_PATH + "/Booking/" + fileName;
            String localFilePath = account.getName() + "/Booking/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx"))
                syncJobData = bookingExcelHelper.getNewBookingFromExcel(syncJob, generalSettings, syncJobType, input);
            else if(bookingConfiguration.fileExtension.equals("xml"))
                syncJobData = bookingExcelHelper.getNewBookingFromXML(syncJob, generalSettings, syncJobType, localFilePath + fileName);

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

    public Response fetchCancelBookingFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        SyncJobType syncJobType;
        SyncJobType newBookingSyncType;
        GeneralSettings generalSettings;
        BookingConfiguration bookingConfiguration;
        try {
            generalSettings = generalSettingsRepo.findByAccountIdAndDeleted(account.getId(), false);
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.CANCEL_BOOKING_REPORT, account.getId(), false);
            newBookingSyncType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.NEW_BOOKING_REPORT, account.getId(), false);
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

        try {
            DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = fileDateFormat.format(new Date());

            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
            String filePath = Constants.REPORTS_BUCKET_PATH + "/CancelBooking/" + fileName;
            String localFilePath = account.getName() + "/CancelBooking/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx"))
                syncJobData = cancelBookingExcelHelper.getCancelBookingFromExcel(syncJob, generalSettings,
                        syncJobType, newBookingSyncType, input);
            else if(bookingConfiguration.fileExtension.equals("xml"))
                syncJobData = cancelBookingExcelHelper.getCancelBookingFromXML(syncJob, generalSettings,
                        syncJobType, newBookingSyncType, localFilePath + fileName);

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

    public Response fetchOccupancyFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        SyncJobType syncJobType;
        BookingConfiguration bookingConfiguration;
        try {
            syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.OCCUPANCY_UPDATE_REPORT, account.getId(), false);
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

        try {
            DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = fileDateFormat.format(new Date());

            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
            String filePath = Constants.REPORTS_BUCKET_PATH + "/Occupancy/" + fileName;
            String localFilePath = account.getName() + "/Occupancy/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);
            List<SyncJobData> syncJobData = new ArrayList<>();

            if(bookingConfiguration.fileExtension.equals("xlsx")){
                syncJobData = excelHelper.getOccupancyFromExcel(syncJob, input);
            } else if(bookingConfiguration.fileExtension.equals("xml")) {
                syncJobData = occupancyXMLHelper.getOccupancyFromXML(syncJob, localFilePath + fileName);
            }

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

    public Response fetchExpensesDetailsFromReport(String userId, Account account) {
        String message = "";
        Response response = new Response();

        SyncJob syncJob;
        BookingConfiguration bookingConfiguration;
        SyncJobType expensesDetailsSyncType;
        GeneralSettings generalSettings;

        try {
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

        try {
            DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = fileDateFormat.format(new Date());
            // fileName = "Expenses20210617.xls"
            String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
            String filePath = Constants.REPORTS_BUCKET_PATH + "/Expenses/" + fileName;
            String localFilePath = account.getName() + "/Expenses/";

            FileInputStream input = downloadFile(fileName, filePath, localFilePath);

            List<SyncJobData> syncJobData = new ArrayList<>();
            if(bookingConfiguration.fileExtension.equals("xlsx")){
                syncJobData = excelHelper.getExpensesUpdateFromXLS(syncJob, input, generalSettings, bookingConfiguration);
            } else if(bookingConfiguration.fileExtension.equals("xml")) {
                syncJobData = expensesXMLHelper.getExpensesUpdateFromXLS(syncJob, localFilePath + fileName,
                        generalSettings, bookingConfiguration);
            }

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

    private FileInputStream downloadFile(String fileName, String filePath, String localFilePath) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new URL(filePath).openStream());

        File file = new File(localFilePath + fileName);
        boolean status = file.getParentFile().mkdirs();
        if (status)
            file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(localFilePath + fileName);

        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }

        return new FileInputStream(file);
    }
}

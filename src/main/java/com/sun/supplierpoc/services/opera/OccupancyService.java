package com.sun.supplierpoc.services.opera;

import com.google.gson.Gson;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.components.ExcelHelper;
import com.sun.supplierpoc.components.OccupancyXMLHelper;
import com.sun.supplierpoc.controllers.opera.MinistryOfTourismResponse;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.Response;
import com.sun.supplierpoc.models.configurations.BookingConfiguration;
import com.sun.supplierpoc.repositories.SyncJobRepo;
import com.sun.supplierpoc.repositories.SyncJobTypeRepo;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class OccupancyService {
    @Autowired
    SyncJobRepo syncJobRepo;

    @Autowired
    SyncJobTypeRepo syncJobTypeRepo;

    @Autowired
    ExcelHelper excelHelper;

    @Autowired
    private SyncJobService syncJobService;
    @Autowired
    private SyncJobDataService syncJobDataService;

    @Autowired
    OccupancyXMLHelper occupancyXMLHelper;

    @Autowired
    BookingService bookingService;

    public SyncJobData createOccupancyObject(String roomsAvailable, String roomsOccupied,
                                             String roomsOnMaintenance, String roomsBooked){
        HashMap<String, Object> data = new HashMap<>();
        int totalRooms = Integer.parseInt(roomsAvailable) + Integer.parseInt(roomsOccupied) +
                Integer.parseInt(roomsOnMaintenance) + Integer.parseInt(roomsBooked);

        Date updateDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        data.put("updateDate", dateFormat.format(updateDate));
        data.put("roomsAvailable", roomsAvailable);
        data.put("roomsOccupied", roomsOccupied);
        data.put("roomsBooked", roomsBooked);
        data.put("roomsOnMaintenance", roomsOnMaintenance);
        data.put("totalRooms", totalRooms);

        SyncJobData syncJobData = new SyncJobData(data, Constants.RECEIVED, "", new Date(), "");

        return syncJobData;
    }

    public Response fetchOccupancyFromReport(String userId, Account account, SyncJobData syncJobData) {
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
            List<SyncJobData> syncJobDataList = new ArrayList<>();
            if(syncJobData != null){
                syncJobData.setSyncJobId(syncJob.getId());
                syncJobDataList.add(syncJobData);
            }else{
                DateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd");
                String currentDate = fileDateFormat.format(new Date());

                String fileName = bookingConfiguration.fileBaseName + currentDate + '.' + bookingConfiguration.fileExtension;
                String filePath = Constants.REPORTS_BUCKET_PATH + account.getName() + "/Occupancy/" + fileName;
                String localFilePath = account.getName() + "/Occupancy/";

                FileInputStream input = bookingService.downloadFile(fileName, filePath, localFilePath);


                if(bookingConfiguration.fileExtension.equals("xlsx")){
                    syncJobDataList = excelHelper.getOccupancyFromExcel(syncJob, input);
                } else if(bookingConfiguration.fileExtension.equals("xml")) {
                    syncJobDataList = occupancyXMLHelper.getOccupancyFromXML(syncJob, localFilePath + fileName);
                }
            }

            /* Send occupancy update */
            response = sendOccupancyUpdates(syncJobDataList, bookingConfiguration);

            if(response.isStatus()){
                syncJobService.saveSyncJobStatus(syncJob, syncJobDataList.size(), response.getMessage(), Constants.SUCCESS);
                syncJobDataService.updateSyncJobDataStatus(syncJobDataList.get(0), Constants.SUCCESS, response.getMessage());
            }else {
                syncJobService.saveSyncJobStatus(syncJob, syncJobDataList.size(), response.getMessage(), Constants.FAILED);
                syncJobDataService.updateSyncJobDataStatus(syncJobDataList.get(0), Constants.FAILED, response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            syncJobService.saveSyncJobStatus(syncJob, 0, e.getMessage(), Constants.FAILED);
            message = "Failed to sync occupancy Updates.";
            response.setMessage(message);
            response.setStatus(false);
        }

        return response;
    }

    private Response sendOccupancyUpdates(List<SyncJobData> syncJobData, BookingConfiguration bookingConfiguration){
        String message = "";
        Response response = new Response();
        try {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(bookingConfiguration.getUsername(), bookingConfiguration.getPassword());

            HashMap<String, Object> data = syncJobData.get(0).getData();

            JSONObject json = new JSONObject();
            json.put("updateDate", String.valueOf(data.get("updateDate")));
            json.put("roomsOccupied", String.valueOf(data.get("roomsOccupied")));
            json.put("roomsAvailable", String.valueOf(data.get("roomsAvailable")));
            json.put("roomsBooked", String.valueOf(data.get("roomsBooked")));
            json.put("roomsOnMaintenance", String.valueOf(data.get("roomsOnMaintenance")));
            json.put("totalRooms", String.valueOf(data.get("totalRooms")));
            json.put("channel", bookingConfiguration.getChannel());
            String body = json.toString();

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, body);

            Request request = new Request.Builder()
                    .url(bookingConfiguration.getUrl())
                    .post(requestBody)
                    .addHeader("X-Gateway-APIKey", bookingConfiguration.getGatewayKey())
                    .addHeader("content-type", "application/json")
                    .addHeader("Authorization", credential)
                    .build();

            okhttp3.Response occupancyUpdateResponse = client.newCall(request).execute();
            if (occupancyUpdateResponse.code() == 200){
                Gson gson = new Gson();
                MinistryOfTourismResponse entity = gson.fromJson(occupancyUpdateResponse.body().string(), MinistryOfTourismResponse.class);

                if(entity.getErrorCode().contains("0")){
                    message = "Occupancy update send successfully.";
                    response.setStatus(true);
                    response.setMessage(message);
                }else{
                    /* Parse Error */
                    message = parseOccupancyErrorMessage(entity.getErrorCode());
                    response.setStatus(false);
                    response.setMessage(message);
                }
            }else {
                message = occupancyUpdateResponse.message();
                response.setStatus(false);
                response.setMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
            response.setStatus(false);
            response.setMessage(message);
        }

        return response;
    }

    private String parseOccupancyErrorMessage(List<String> errorCodes){
        String message = "";
        String code = errorCodes.get(0);
        switch (code) {
            case "1":
                message = "Invalid Update Date, It should be numeric in following format YYYYMMDD.";
                break;
            case "2":
                message = "Invalid Rooms Occupied. It must be numeric only. It can contain 0.";
                break;
            case "3":
                message = "Invalid Rooms Available. It must be numeric only. It can contain 0.";
                break;
            case "4":
                message = "Invalid Rooms Booked. It must be numeric only. It can contain 0.";
                break;
            case "5":
                message = "Invalid Rooms on Maintenance. It must be numeric only. It can contain 0.";
                break;
            case "6":
                message = "Invalid UserId or User Id not found.";
                break;
            case "7":
                message = "Invalid Transaction Id or TransactionId not found.";
                break;
            case "8":
                message = "Invalid Day Closing value. It should be true or false.";
                break;
            case "9":
                message = "Invalid Total Rooms value. It must be numeric only. It can contain 0.";
                break;
            case "10":
                message = "Incorrect Total Rooms Value.";
                break;
            case "11":
                message = "Invalid Total Adults value.";
                break;
            case "12":
                message = "Invalid Total Children value.";
                break;
            case "13":
                message = "Invalid Total Guests value.";
                break;
            case "14":
                message = "Incorrect Total Guests value.";
                break;
            case "15":
                message = "Total Guests is mandatory if day closing is true.";
                break;
            case "16":
                message = "Invalid Total Revenue value.";
                break;
            case "17":
                message = "Total Revenue is mandatory if day closing is true.";
                break;
            case "100":
                message = "Invalid Credentials. Authentication failed.";
                break;
            case "101":
                message = "Internal Server Error. Please try again later.";
                break;
        }

        return message;
    }

}

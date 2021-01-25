package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.controllers.simphony.MenuItemsController;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.simphony.check.ZealPayment;
import com.sun.supplierpoc.models.simphony.check.ZealPoints;
import com.sun.supplierpoc.models.simphony.check.ZealVoucher;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class ZealService {

    Logger logger = LoggerFactory.getLogger(ZealService.class);


    @Autowired
    private SyncJobRepo syncJobRepo;

    @Autowired
    MenuItemService menuItemService;
    @Autowired
    SyncJobService syncJobService;
    @Autowired
    SyncJobDataService syncJobDataService;
    @Autowired
    AccountService accountService;
    @Autowired
    InvokerUserService invokerUserService;
    @Autowired
    private OperationTypeRepo operationTypeRepo;
    @Autowired
    private SyncJobDataRepo syncJobDataRepo;
    @Autowired
    private OperationRepo operationRepo;
    @Autowired
    private OperationDataRepo operationDataRepo;

    public Response zealPaymentService(ZealPayment zealPayment, String userId, Account account, int revenueCenterID) {

        Response response = new Response();

        Operation operation = null;
        try {

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Payment", account.getId(), false);


            operation = new Operation(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), operationType.getId(), revenueCenterID, false);

            operationRepo.save(operation);
            response.setStatus(true);

            if (response.isStatus()) {

                // Save menu items
                ArrayList<OperationData> savedMenuItems = saveZealPayment(zealPayment, operation);

                operation.setStatus(Constants.SUCCESS);
                operation.setEndDate(new Date());
                operation.setRowsFetched(response.getMenuItems().size());
                operationRepo.save(operation);
                logger.info(savedMenuItems.get(0).getOperationId());
                response.setAddedOperationData(savedMenuItems);
            } else {
                operation.setStatus(Constants.FAILED);
                operation.setReason(response.getMessage());
                operation.setEndDate(new Date());
                operation.setRowsFetched(0);
                operationRepo.save(operation);
            }

            return response;
        } catch (Exception e) {

            if (operation != null) {
                operation.setStatus(Constants.FAILED);
                operation.setReason(e.getMessage());
                operation.setEndDate(new Date());
                operation.setRowsFetched(0);
                operationRepo.save(operation);
            }
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    public ArrayList<OperationData> saveZealPayment(ZealPayment zealPayment, Operation operation) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();

        HashMap<String, Object> zealPaymentData = new HashMap<>();

        zealPaymentData.put("id", Integer.toString(zealPayment.getId()));
        zealPaymentData.put("code", zealPayment.getCode());
        zealPaymentData.put("totalDue", zealPayment.getTotalDue());
        zealPaymentData.put("message", zealPayment.getMessage());

        String status = zealPayment.getStatus();

        if (!zealPayment.getStatus().equals("success"))
            status = Constants.FAILED;

        OperationData operationData = new OperationData(zealPaymentData, status, "", new Date(),
                operation.getId());

        operationDataRepo.save(operationData);

        savedMenuItems.add(operationData);

        return savedMenuItems;
    }

    public Response simphonyZealVoucher(ZealVoucher zealVoucher, String username, String userId, Account account, int revenueCenterID) {

        logger.info("get method");

        Response response = new Response();

        SyncJob syncJob = null;

        try {

            logger.info("get try");

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Voucher", account.getId(), false);

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), operationType.getId(), 0);

            syncJob.setRevenueCenter(revenueCenterID);

            syncJobRepo.save(syncJob);

            response.setStatus(true);

            if (response.isStatus()) {

                HashMap<String, String> map = getItem();

                ArrayList<SyncJobData> savedMenuItems = saveZealVoucher(map, zealVoucher, syncJob);


                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(response.getMenuItems().size());
                syncJobRepo.save(syncJob);

                response.setAddedSyncJobData(savedMenuItems);
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(response.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }

            return response;
        } catch (Exception e) {

            if (syncJob != null) {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(e.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    public ArrayList<SyncJobData> saveZealVoucher(HashMap<String, String> map1, ZealVoucher zealVoucher, SyncJob syncJob) {


        ArrayList<SyncJobData> savedMenuItems = new ArrayList<>();

        HashMap<String, Object> zealPaymentData = new HashMap<>();

        HashMap<String, String> map = new HashMap<>();

        zealPaymentData.put("id", Integer.toString(zealVoucher.getId()));
        zealPaymentData.put("code", zealVoucher.getCode());
        zealPaymentData.put("itemId", zealVoucher.getItemId());
        zealPaymentData.put("message", zealVoucher.getMessage());
        zealPaymentData.put("data2", map);

        String status = Constants.FAILED;

        if (zealVoucher.getStatus().equals("success")) {
            map = map1;
            zealPaymentData.put("data2", map);
            status = Constants.SUCCESS;
        }

        SyncJobData syncJobData = new SyncJobData(status, zealPaymentData, "", new Date(),
                syncJob.getId());

        syncJobDataRepo.save(syncJobData);

        savedMenuItems.add(syncJobData);

        return savedMenuItems;
    }

    public Response simphonyZealPoints(ZealPoints zealPoints, String userId, Account account, int revenueCenterID) {

        Response response = new Response();

        SyncJob syncJob = null;

        try {

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Points", account.getId(), false);

            syncJob = new SyncJob(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), operationType.getId(), 0);

            syncJob.setRevenueCenter(revenueCenterID);

            syncJobRepo.save(syncJob);

            response.setStatus(true);

            if (response.isStatus()) {

                // Save menu items
                ArrayList<SyncJobData> savedMenuItems = saveZealPoints(zealPoints, syncJob);

                syncJob.setStatus(Constants.SUCCESS);
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(response.getMenuItems().size());
                syncJobRepo.save(syncJob);

                response.setAddedSyncJobData(savedMenuItems);
            } else {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(response.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }

            return response;
        } catch (Exception e) {

            if (syncJob != null) {
                syncJob.setStatus(Constants.FAILED);
                syncJob.setReason(e.getMessage());
                syncJob.setEndDate(new Date());
                syncJob.setRowsFetched(0);
                syncJobRepo.save(syncJob);
            }
            e.printStackTrace();
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    public ArrayList<SyncJobData> saveZealPoints(ZealPoints zealPoints, SyncJob syncJob) {

        ArrayList<SyncJobData> savedMenuItems = new ArrayList<>();

        HashMap<String, String> zealPaymentData = new HashMap<>();

        int coma = zealPoints.getTotalDue().indexOf(".");
        String totalPoints = zealPoints.getTotalDue();

        if (coma != -1)
            totalPoints = zealPoints.getTotalDue().substring(0, coma);

        zealPaymentData.put("id", Integer.toString(zealPoints.getId()));
        zealPaymentData.put("code", zealPoints.getCode());
        zealPaymentData.put("totalDue", totalPoints);
        zealPaymentData.put("message", zealPoints.getMessage());

        String status = zealPoints.getStatus();

        if (!zealPoints.getStatus().equals("success"))
            status = Constants.FAILED;

        SyncJobData syncJobData = new SyncJobData(zealPaymentData, status, "", new Date(),
                syncJob.getId());
        try {
            syncJobDataRepo.save(syncJobData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedMenuItems.add(syncJobData);

        return savedMenuItems;
    }

    public Response osimphonyZealPayment(ZealPayment zealPayment, String userId, Account account,
                                         int revenueCenterID) {

        Response response = new Response();

        OperationType operationType = null;

        Operation operation = null;
        try {

            operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Payment", account.getId(), false);

            operation = new Operation(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), operationType.getId(), revenueCenterID, false);

            operationRepo.save(operation);

            response.setStatus(true);

            if (response.isStatus()) {

                // Save menu items
                ArrayList<OperationData> savedMenuItems = osaveZealPayment(zealPayment, operation);

                operation.setStatus(Constants.SUCCESS);
                operation.setEndDate(new Date());
                operation.setRowsFetched(response.getMenuItems().size());
                operationRepo.save(operation);

                response.setAddedOperationData(savedMenuItems);
            } else {
                operation.setStatus(Constants.FAILED);
                operation.setReason(response.getMessage());
                operation.setEndDate(new Date());
                operation.setRowsFetched(0);
                operationRepo.save(operation);
            }

            return response;
        } catch (Exception e) {

            if (operation != null) {
                operation.setStatus(Constants.FAILED);
                operation.setReason(e.getMessage());
                operation.setEndDate(new Date());
                operation.setRowsFetched(0);
                operationRepo.save(operation);
            }
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    public ArrayList<OperationData> osaveZealPayment(ZealPayment zealPayment, Operation operation) {

        ArrayList<OperationData> savedOperationData = new ArrayList<>();

        HashMap<String, Object> zealPaymentData = new HashMap<>();

        zealPaymentData.put("id", Integer.toString(zealPayment.getId()));
        zealPaymentData.put("code", zealPayment.getCode());
        zealPaymentData.put("totalDue", zealPayment.getTotalDue());

        OperationData operationData = new OperationData(zealPaymentData, Constants.RECEIVED, "", new Date(),
                operation.getId());

        operationDataRepo.save(operationData);

        savedOperationData.add(operationData);


        return savedOperationData;
    }

    public HashMap<String, String> getItem() {

        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted("60070927e5bc3923a380c51a", false));

        ArrayList<HashMap<String, String>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);

        //       HashMap<String, String> map = menuItems.get(1);
        HashMap<String, String> map = new HashMap<>();
        map.put("menuFirstName", "IFC Test Item - SI 3");
        map.put("menuItemPrice", "3");
        map.put("availability", "true");

        return map;

    }

}

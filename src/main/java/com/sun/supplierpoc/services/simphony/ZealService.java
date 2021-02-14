package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.ZealRedeemResponse;
import com.sun.supplierpoc.models.simphony.check.ZealPayment;
import com.sun.supplierpoc.models.simphony.check.ZealPoints;
import com.sun.supplierpoc.models.simphony.check.ZealVoucher;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import com.sun.supplierpoc.soapModels.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class ZealService {

    Logger logger = LoggerFactory.getLogger(ZealService.class);

    @Autowired
    private CallRestService callRestService;
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

    public ZealLoyaltyResponse zealPaymentProcessor(ZealPayment zealPayment, InvokerUser user, Account account, SimphonyLocation location) {

        ZealLoyaltyResponse response = new ZealLoyaltyResponse();

        Operation operation = null;
        try {

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Payment", account.getId(), false);

            operation = new Operation(Constants.RUNNING, "", new Date(), null, user.getId(),
                    account.getId(), operationType.getId(), location.getRevenueCenterID(), false);

            if (!user.getTypeId().equals(operationType.getId())){
                throw new Exception("You don't have role to check loyalty!");
            }

            operationRepo.save(operation);

            logger.info(zealPayment.toString());

            ZealLoyaltyRequest zealLoyaltyRequest = new ZealLoyaltyRequest(zealPayment.getCode()
                    , Double.parseDouble(zealPayment.getTotalDue()), Double.parseDouble(zealPayment.getCheckNumber()));

            response = callRestService.zealPayment(zealLoyaltyRequest);

            if (response.isLoyalty()) {

                ArrayList<OperationData> savedMenuItems = saveZealPayment(zealPayment, operation);
                operation.setStatus(Constants.SUCCESS);
                operation.setEndDate(new Date());
                operationRepo.save(operation);
                logger.info(savedMenuItems.get(0).getOperationId());
                response.setStatus("Success");
                response.setAddedOperationData(savedMenuItems);
            } else {
                ArrayList<OperationData> savedMenuItems = saveZealPayment(zealPayment, operation);
                operation.setStatus(Constants.FAILED);
                operation.setReason(response.getMessage());
                operation.setEndDate(new Date());
                response.setStatus("Success");
                operationRepo.save(operation);
            }

            return response;
        } catch (Exception e) {

            if (operation != null) {
                operation.setStatus(Constants.FAILED);
                operation.setReason(e.getMessage());
                operation.setEndDate(new Date());
                operationRepo.save(operation);
            }
            response.setMessage(e.getMessage());
            response.setStatus("Failed");
            return response;
        }
    }

    public ArrayList<OperationData> saveZealPayment(ZealPayment zealPayment, Operation operation) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();

        HashMap<String, Object> zealPaymentData = new HashMap<>();

        zealPaymentData.put("id", Integer.toString(zealPayment.getId()));
        zealPaymentData.put("code", zealPayment.getCode());
        zealPaymentData.put("totalDue", zealPayment.getTotalDue());
        zealPaymentData.put("receiptNumber", zealPayment.getCheckNumber());
        zealPaymentData.put("status", zealPayment.getStatus());
        zealPaymentData.put("message", zealPayment.getMessage());

        OperationData operationData = new OperationData(zealPaymentData, new Date(),
                operation.getId());

        operationDataRepo.save(operationData);

        savedMenuItems.add(operationData);

        return savedMenuItems;
    }

    public ZealRedeemResponse zealVoucherProcessor(ZealVoucher zealVoucher, InvokerUser user, Account account, SimphonyLocation location) {

        ZealRedeemResponse response = new ZealRedeemResponse();
        Operation operation = null;
        try {
            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Voucher", account.getId(), false);

            operation = new Operation(Constants.RUNNING, "", new Date(), null, user.getId(),
                    account.getId(), operationType.getId(), location.getRevenueCenterID(), false);

            if (!user.getTypeId().equals(operationType.getId())){
                 throw new Exception("You don't have role to create check!");
            }

            operationRepo.save(operation);

            ZealRedeemRequest zealRedeemRequest = new ZealRedeemRequest(zealVoucher.getCode());
            response = callRestService.zealVoucher(zealRedeemRequest);
            HashMap<String, String> map = getItem();
            ArrayList<OperationData> savedMenuItems = saveZealVoucher(map, zealVoucher, operation);
            operation.setStatus(Constants.SUCCESS);
            operation.setEndDate(new Date());
            operationRepo.save(operation);
            response.setStatus(true);
            response.setAddedOperationData(savedMenuItems);
            operationRepo.save(operation);

            return response;
        } catch (Exception e) {

            if (operation != null) {
                operation.setStatus(Constants.FAILED);
                operation.setReason(e.getMessage());
                operation.setEndDate(new Date());
                operation.setRowsFetched(0);
                operationRepo.save(operation);
            }
            response.setStatus(false);
            return response;
        }
    }

    public ArrayList<OperationData> saveZealVoucher(HashMap<String, String> map1, ZealVoucher zealVoucher, Operation operation) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();
        HashMap<String, Object> zealPaymentData = new HashMap<>();
        HashMap<String, String> map = new HashMap<>();
        zealPaymentData.put("id", Integer.toString(zealVoucher.getId()));
        zealPaymentData.put("code", zealVoucher.getCode());
        zealPaymentData.put("itemId", zealVoucher.getItemId());
        zealPaymentData.put("status", zealVoucher.getStatus());
        zealPaymentData.put("message", zealVoucher.getMessage());
        zealPaymentData.put("data2", map1);

        OperationData operationData = new OperationData(zealPaymentData, new Date(),
                operation.getId());

        operationDataRepo.save(operationData);

        savedMenuItems.add(operationData);

        return savedMenuItems;
    }

    public Response zealPointsProcessor(ZealPoints zealPoints, String userId, Account account, int revenueCenterID) {

        Response response = new Response();

        Operation operation = null;

        try {

            OperationType operationType = operationTypeRepo.findAllByNameAndAccountIdAndDeleted("Zeal Points", account.getId(), false);

            operation = new Operation(Constants.RUNNING, "", new Date(), null, userId,
                    account.getId(), operationType.getId(), revenueCenterID, false);


            operationRepo.save(operation);

            response.setStatus(true);

            if (response.isStatus()) {

                // Save menu items
                ArrayList<OperationData> savedMenuItems = saveZealPoints(zealPoints, operation);

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
            e.printStackTrace();
            response.setMessage(e.getMessage());
            response.setStatus(false);

            return response;
        }
    }

    public ArrayList<OperationData> saveZealPoints(ZealPoints zealPoints, Operation operation) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();

        HashMap<String, Object> zealPaymentData = new HashMap<>();

        int coma = zealPoints.getTotalDue().indexOf(".");
        String totalPoints = zealPoints.getTotalDue();

        if (coma != -1)
            totalPoints = zealPoints.getTotalDue().substring(0, coma);

        zealPaymentData.put("id", Integer.toString(zealPoints.getId()));
        zealPaymentData.put("code", zealPoints.getCode());
        zealPaymentData.put("totalDue", totalPoints);
        zealPaymentData.put("message", zealPoints.getMessage());
        OperationData operationData = new OperationData( zealPaymentData, new Date(),
                operation.getId());
        operationDataRepo.save(operationData);
        savedMenuItems.add(operationData);
        return savedMenuItems;
    }

    public HashMap<String, String> getItem() {

        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted("60070927e5bc3923a380c51a", false));

        ArrayList<HashMap<String, String>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);

        HashMap<String, String> map = new HashMap<>();
        map.put("itemObjectId", "4");
        map.put("menuFirstName", "IFC Test Item - SI 3");
        map.put("menuItemPrice", "3");
        map.put("availability", "true");

        return map;

    }

}

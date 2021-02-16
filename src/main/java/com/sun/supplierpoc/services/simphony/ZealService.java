package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.auth.InvokerUser;
import com.sun.supplierpoc.models.configurations.SimphonyLocation;
import com.sun.supplierpoc.models.simphony.request.ZealLoyaltyRequest;
import com.sun.supplierpoc.models.simphony.response.ZealLoyaltyResponse;
import com.sun.supplierpoc.models.simphony.request.ZealRedeemRequest;
import com.sun.supplierpoc.models.simphony.response.ZealRedeemResponse;
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
                throw new Exception("You don't have role to add loyalty!");
            }

            operationRepo.save(operation);

            ZealLoyaltyRequest zealLoyaltyRequest = new ZealLoyaltyRequest(zealPayment.getCode()
                    , Double.parseDouble(zealPayment.getTotalDue()), Double.parseDouble(zealPayment.getCheckNumber()));

            response = callRestService.zealPayment(zealLoyaltyRequest);

            if (response.isLoyalty()) {

                ArrayList<OperationData> savedMenuItems = saveZealPayment(zealPayment, operation);
                operation.setStatus(Constants.SUCCESS);
                operation.setEndDate(new Date());
                operationRepo.save(operation);
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
                 throw new Exception("You don't have role to redeem reward!");
            }

            operationRepo.save(operation);

            ZealRedeemRequest zealRedeemRequest = new ZealRedeemRequest(zealVoucher.getCode());
            response = callRestService.zealVoucher(zealRedeemRequest);
            HashMap<String, String> map = getItem(response.getMenuItemId());

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
        zealPaymentData.put("checkNumber", zealVoucher.getCheckNumber());
        zealPaymentData.put("status", zealVoucher.getStatus());
        zealPaymentData.put("message", zealVoucher.getMessage());
        zealPaymentData.put("menuItems", map1);

        OperationData operationData = new OperationData(zealPaymentData, new Date(),
                operation.getId());

        operationDataRepo.save(operationData);

        savedMenuItems.add(operationData);
        System.out.println(savedMenuItems);

        return savedMenuItems;
    }

    public HashMap<String, String> getItem(String itemId) {

        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc("5fe34649283cde246c2d7736", false);
        SyncJob syncJob = syncJobs.get(0);
        System.out.println(syncJob.getEndDate());
        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false));

        ArrayList<HashMap<String, String>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);

        HashMap<String, String> menuItemsMap = new HashMap<>();
        menuItemsMap.put("itemObjectId", "4");
        menuItemsMap.put("menuFirstName", "IFC Test Item - SI 3");
        menuItemsMap.put("menuItemPrice", "3");
        menuItemsMap.put("availability", "true");

//        for (HashMap<String, String> tempItem : menuItems) {
//
//            if (tempItem.get("miObjectNum").equals(itemId)) {
//                menuItemsMap = tempItem;
//            }
//
//        }
        return menuItemsMap;

    }

}

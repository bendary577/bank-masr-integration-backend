package com.sun.supplierpoc.services.simphony;

import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.*;
import com.sun.supplierpoc.models.simphony.MenuItem;
import com.sun.supplierpoc.models.simphony.SimphonyMenuItem;
import com.sun.supplierpoc.models.simphony.transaction.PostTransactionEx2;
import com.sun.supplierpoc.repositories.*;
import com.sun.supplierpoc.services.AccountService;
import com.sun.supplierpoc.services.InvokerUserService;
import com.sun.supplierpoc.services.SyncJobDataService;
import com.sun.supplierpoc.services.SyncJobService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CreateOrderService {

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
    @Autowired
    private SyncJobTypeRepo syncJobTypeRepo;


    public ArrayList<OperationData> saveOrderCreation(PostTransactionEx2 checkDetails, Operation operation, String accountId) {

        ArrayList<OperationData> savedMenuItems = new ArrayList<>();
        HashMap<String, Object> zealPaymentData = new HashMap<>();
        List<HashMap<String, String>> menuItemsMap = getItem(checkDetails, accountId);

        zealPaymentData.put("checkOrderType", checkDetails.getpGuestCheck().getCheckOrderType());
        zealPaymentData.put("checkInfoLines", checkDetails.getpGuestCheck().getPCheckInfoLines());
        zealPaymentData.put("status", "received");
        zealPaymentData.put("menuItems", menuItemsMap);

        LoggerFactory.getLogger(CreateOrderService.class).info(zealPaymentData.toString());
        OperationData operationData = new OperationData(zealPaymentData, new Date(),
                operation.getId());

        operationDataRepo.save(operationData);
        savedMenuItems.add(operationData);
        return savedMenuItems;
    }

    private List<HashMap<String, String>> getItem(PostTransactionEx2 checkDetails, String accountId) {
        SyncJobType syncJobType = syncJobTypeRepo.findByNameAndAccountIdAndDeleted(Constants.MENU_ITEMS, accountId, false);

        List<SyncJob> syncJobs = syncJobRepo.findBySyncJobTypeIdAndDeletedOrderByCreationDateDesc(syncJobType.getId(), false);
        SyncJob syncJob = syncJobs.get(0);

        ArrayList<SyncJobData> syncJobData = new ArrayList<>(syncJobDataRepo.findBySyncJobIdAndDeleted(syncJob.getId(), false));

        ArrayList<HashMap<String, String>> menuItems = menuItemService.simplifyMenuItemData(syncJobData);
        List<HashMap<String, String>> menuItemsMap = new ArrayList<>();

        List<SimphonyMenuItem> listMenuItem = checkDetails.getPpMenuItemsEx();

        for (SimphonyMenuItem menuItem : listMenuItem) {
            for (HashMap<String, String> tempItem : menuItems) {

                if (tempItem.get("miObjectNum").equals(menuItem.getMenuItem().getMiObjectNum())) {
                    menuItemsMap.add(tempItem);
                }
            }
        }
        return menuItemsMap;
    }

}